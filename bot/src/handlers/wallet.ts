import { Telegraf } from 'telegraf';
import { BotContext, requireAuth } from '../middlewares/auth';
import { apiService } from '../services/api';
import { userService } from '../services/user';
import { walletKeyboard, rechargeMethodKeyboard, backKeyboard } from '../keyboards/main';
import { formatMoney, errorMessage } from '../utils/format';

export function registerWalletHandler(bot: Telegraf<BotContext>) {
  // /balance 命令
  bot.command('balance', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    try {
      const res = await apiService.getBalance(session.token, session.userId);
      if (res.code === 200) {
        const balance = res.data?.balance ?? session.balance;
        userService.updateBalance(session.telegramId, balance);
        await ctx.reply(
          `💰 *账户余额*\n\n当前余额：${formatMoney(balance)}\n\n可用于充值、提现或游戏。`,
          { parse_mode: 'Markdown', ...walletKeyboard() }
        );
      } else {
        await ctx.reply(errorMessage(res.msg || '查询失败'));
      }
    } catch (err) {
      console.error('[Balance] 错误:', err);
      await ctx.reply(errorMessage('网络异常，请稍后重试'));
    }
  });

  // 余额查询回调
  bot.action('wallet_balance', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    try {
      const res = await apiService.getBalance(session.token, session.userId);
      const balance = res.code === 200 ? (res.data?.balance ?? session.balance) : session.balance;
      if (res.code === 200) userService.updateBalance(session.telegramId, balance);
      await ctx.editMessageText(
        `💰 *账户余额*\n\n当前余额：${formatMoney(balance)}`,
        { parse_mode: 'Markdown', ...walletKeyboard() }
      );
    } catch (err) {
      await ctx.editMessageText(errorMessage('查询失败，请稍后重试'));
    }
    await ctx.answerCbQuery();
  });

  // 充值
  bot.action('wallet_recharge', async (ctx) => {
    await ctx.editMessageText(
      '💳 *充值*\n\n请选择充值方式：',
      { parse_mode: 'Markdown', ...rechargeMethodKeyboard() }
    );
    await ctx.answerCbQuery();
  });

  // 充值方式选择
  const rechargeMethods: Record<string, string> = {
    recharge_bank: '🏦 银行卡转账',
    recharge_usdt: '💳 USDT (TRC20)',
    recharge_online: '📱 在线支付',
  };

  Object.keys(rechargeMethods).forEach((key) => {
    bot.action(key, async (ctx) => {
      if (!requireAuth(ctx)) return;
      const session = ctx.session!;
      try {
        // 创建充值订单（默认金额，实际应由用户输入）
        const res = await apiService.createRecharge(session.token, session.userId, 100, key.replace('recharge_', ''));
        if (res.code === 200 && res.data?.payUrl) {
          await ctx.editMessageText(
            `✅ *充值订单已创建*\n\n` +
            `方式：${rechargeMethods[key]}\n` +
            `金额：${formatMoney(100)}\n` +
            `订单号：${res.data.orderNo || '-'}\n\n` +
            `请点击下方按钮完成支付：`,
            {
              parse_mode: 'Markdown',
              reply_markup: {
                inline_keyboard: [
                  [{ text: '🔗 前往支付', url: res.data.payUrl }],
                  [{ text: '🔙 返回钱包', callback_data: 'wallet_menu' }],
                ],
              },
            }
          );
        } else {
          await ctx.editMessageText(errorMessage(res.msg || '创建订单失败'));
        }
      } catch (err) {
        console.error('[Recharge] 错误:', err);
        await ctx.editMessageText(errorMessage('充值服务暂不可用，请稍后重试或进入游戏内充值'));
      }
      await ctx.answerCbQuery();
    });
  });

  // 提现
  bot.action('wallet_withdraw', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    await ctx.editMessageText(
      `📤 *提现申请*\n\n` +
      `可提现余额：${formatMoney(session.balance)}\n\n` +
      `请进入游戏内提交提现申请，支持银行卡和USDT提现。\n` +
      `提现处理时间：1-2小时内到账。`,
      {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [
            [{ text: '🚀 进入游戏提现', web_app: { url: process.env.MINI_APP_URL || '' } }],
            [{ text: '🔙 返回钱包', callback_data: 'wallet_menu' }],
          ],
        },
      }
    );
    await ctx.answerCbQuery();
  });

  // 交易明细
  bot.action('wallet_transactions', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    try {
      const res = await apiService.getTransactions(session.token, session.userId, 1, 5);
      if (res.code === 200 && res.data?.list?.length > 0) {
        const list = res.data.list.slice(0, 5);
        const text = list.map((item: any) => {
          const typeText = item.type === 1 ? '充值' : item.type === 2 ? '提现' : item.type === 3 ? '消费' : '其他';
          const amountText = item.type === 1 ? `+${formatMoney(item.amount)}` : `-${formatMoney(item.amount)}`;
          return `${typeText} ${amountText}\n${item.createTime || ''} ${item.remark || ''}`;
        }).join('\n\n');
        await ctx.editMessageText(`📊 *最近交易*\n\n${text}`, {
          parse_mode: 'Markdown',
          ...backKeyboard('wallet_menu'),
        });
      } else {
        await ctx.editMessageText('📊 *最近交易*\n\n暂无交易记录。', {
          parse_mode: 'Markdown',
          ...backKeyboard('wallet_menu'),
        });
      }
    } catch (err) {
      await ctx.editMessageText(errorMessage('查询失败，请稍后重试'));
    }
    await ctx.answerCbQuery();
  });
}
