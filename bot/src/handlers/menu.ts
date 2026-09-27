import { Telegraf } from 'telegraf';
import { BotContext, requireAuth } from '../middlewares/auth';
import {
  mainMenuKeyboard,
  walletKeyboard,
  gameKeyboard,
  promoKeyboard,
  supportKeyboard,
} from '../keyboards/main';
import { mainMenuMessage } from '../utils/format';

export function registerMenuHandler(bot: Telegraf<BotContext>) {
  // /menu 命令
  bot.command('menu', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    await ctx.reply(
      mainMenuMessage(session.nickname, session.balance, session.vipLevel),
      { parse_mode: 'Markdown', ...mainMenuKeyboard() }
    );
  });

  // 主菜单回调
  bot.action('main_menu', async (ctx) => {
    if (!requireAuth(ctx)) return;
    const session = ctx.session!;
    await ctx.editMessageText(
      mainMenuMessage(session.nickname, session.balance, session.vipLevel),
      { parse_mode: 'Markdown', ...mainMenuKeyboard() }
    );
    await ctx.answerCbQuery();
  });

  // 钱包菜单
  bot.action('wallet_menu', async (ctx) => {
    await ctx.editMessageText('💰 *钱包管理*\n\n请选择操作：', {
      parse_mode: 'Markdown',
      ...walletKeyboard(),
    });
    await ctx.answerCbQuery();
  });

  // 游戏菜单
  bot.action('game_menu', async (ctx) => {
    await ctx.editMessageText('🎮 *游戏中心*\n\n选择游戏快速进入：', {
      parse_mode: 'Markdown',
      ...gameKeyboard(),
    });
    await ctx.answerCbQuery();
  });

  // 活动菜单
  bot.action('promo_menu', async (ctx) => {
    await ctx.editMessageText('🎁 *活动中心*\n\n选择活动：', {
      parse_mode: 'Markdown',
      ...promoKeyboard(),
    });
    await ctx.answerCbQuery();
  });

  // 客服菜单
  bot.action('support', async (ctx) => {
    await ctx.editMessageText('💬 *在线客服*\n\n请选择问题类型：', {
      parse_mode: 'Markdown',
      ...supportKeyboard(),
    });
    await ctx.answerCbQuery();
  });

  // 游戏规则
  bot.action('rules', async (ctx) => {
    await ctx.editMessageText(
      '📖 *游戏规则*\n\n' +
      '1. 本平台仅供娱乐，请理性游戏\n' +
      '2. 所有游戏结果以系统开奖为准\n' +
      '3. 充值后即时到账，提现1-2小时内处理\n' +
      '4. 严禁使用外挂、作弊等违规行为\n' +
      '5. 如有疑问请联系在线客服\n\n' +
      '详细规则请进入游戏内查看。',
      { parse_mode: 'Markdown', ...{ reply_markup: { inline_keyboard: [[{ text: '🔙 返回主菜单', callback_data: 'main_menu' }]] } } }
    );
    await ctx.answerCbQuery();
  });

  // 设置
  bot.action('settings', async (ctx) => {
    const session = ctx.session;
    await ctx.editMessageText(
      '⚙️ *设置*\n\n' +
      `👤 用户名：${session?.nickname || '-'}\n` +
      `🆔 用户ID：${session?.userId || '-'}\n` +
      `👑 等级：${session ? `VIP${session.vipLevel}` : '-'}\n\n` +
      '更多设置请进入游戏内调整。',
      { parse_mode: 'Markdown', ...{ reply_markup: { inline_keyboard: [[{ text: '🔙 返回主菜单', callback_data: 'main_menu' }]] } } }
    );
    await ctx.answerCbQuery();
  });
}
