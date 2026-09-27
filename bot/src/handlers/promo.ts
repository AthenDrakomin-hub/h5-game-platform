import { Telegraf } from 'telegraf';
import { BotContext, requireAuth } from '../middlewares/auth';
import { apiService } from '../services/api';
import { config } from '../config';
import { backKeyboard } from '../keyboards/main';
import { formatMoney, errorMessage } from '../utils/format';

export function registerPromoHandler(bot: Telegraf<BotContext>) {
  // 每日签到
  bot.action('promo_signin', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    try {
      const statusRes = await apiService.getSignInStatus(session.token, session.userId);
      const isSigned = statusRes.code === 200 && statusRes.data?.todaySigned;
      const reward = statusRes.data?.todayReward || 8.88;
      const continuousDays = statusRes.data?.continuousDays || 0;

      if (isSigned) {
        await ctx.editMessageText(
          `📅 *每日签到*\n\n✅ 今日已签到\n连续签到：${continuousDays}天\n今日奖励：${formatMoney(reward)}\n\n明天记得继续签到哦！`,
          { parse_mode: 'Markdown', ...backKeyboard('main_menu') }
        );
      } else {
        const signRes = await apiService.dailySignIn(session.token, session.userId);
        if (signRes.code === 200) {
          const rewardAmount = signRes.data?.reward || reward;
          await ctx.editMessageText(
            `🎉 *签到成功*\n\n获得奖励：${formatMoney(rewardAmount)}\n连续签到：${continuousDays + 1}天\n\n奖励已发放至账户余额。`,
            { parse_mode: 'Markdown', ...backKeyboard('main_menu') }
          );
        } else {
          await ctx.editMessageText(errorMessage(signRes.msg || '签到失败'));
        }
      }
    } catch (err) {
      console.error('[SignIn] 错误:', err);
      await ctx.editMessageText(errorMessage('签到服务暂不可用，请进入游戏内签到'));
    }
    await ctx.answerCbQuery();
  });

  // 活动列表
  bot.action('promo_list', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    try {
      const res = await apiService.getPromoList(session.token, session.userId);
      if (res.code === 200 && res.data?.list?.length > 0) {
        const list = res.data.list.slice(0, 5);
        const text = list.map((item: any, i: number) => {
          return `${i + 1}. *${item.title}*\n${item.description || ''}\n`;
        }).join('\n');
        await ctx.editMessageText(`🎁 *优惠活动*\n\n${text}`, {
          parse_mode: 'Markdown',
          reply_markup: {
            inline_keyboard: [
              [{ text: '🚀 查看详情', web_app: { url: config.miniAppUrl } }],
              [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
            ],
          },
        });
      } else {
        await ctx.editMessageText(
          '🎁 *优惠活动*\n\n暂无进行中的活动，请关注后续公告。',
          { parse_mode: 'Markdown', ...backKeyboard('main_menu') }
        );
      }
    } catch (err) {
      await ctx.editMessageText(errorMessage('获取活动列表失败'));
    }
    await ctx.answerCbQuery();
  });

  // 邀请返利
  bot.action('promo_invite', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    const inviteLink = `https://t.me/${config.botUsername.replace('@', '')}?start=invite_${session.userId}`;
    await ctx.editMessageText(
      `👥 *邀请返利*\n\n` +
      `邀请好友注册，好友首次充值您可获得返利！\n\n` +
      `您的邀请链接：\n\`${inviteLink}\`\n\n` +
      `分享给好友，好友通过链接注册即可绑定邀请关系。`,
      {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [
            [{ text: '📤 分享邀请链接', switch_inline_query: `快来NOVA新星娱乐，注册即送好礼！${inviteLink}` }],
            [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
          ],
        },
      }
    );
    await ctx.answerCbQuery();
  });

  // VIP 特权
  bot.action('promo_vip', async (ctx) => {
    await ctx.editMessageText(
      '👑 *VIP 特权*\n\n' +
      'VIP1：每日签到翻倍 + 专属客服\n' +
      'VIP2：提现免手续费 + 生日礼金\n' +
      'VIP3：专属客户经理 + 高额返利\n' +
      'VIP4：极速提现通道 + 定制活动\n' +
      'VIP5：全部特权 + 线下活动邀请\n\n' +
      '累计充值自动升级VIP等级。',
      {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [
            [{ text: '💰 充值升级VIP', callback_data: 'wallet_recharge' }],
            [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
          ],
        },
      }
    );
    await ctx.answerCbQuery();
  });
}
