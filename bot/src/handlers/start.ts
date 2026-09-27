import { Telegraf } from 'telegraf';
import { BotContext, requireAuth } from '../middlewares/auth';
import { mainMenuKeyboard } from '../keyboards/main';
import { welcomeMessage, errorMessage } from '../utils/format';

export function registerStartHandler(bot: Telegraf<BotContext>) {
  bot.start(async (ctx) => {
    if (!requireAuth(ctx)) return;

    try {
      const session = ctx.session!;
      await ctx.replyWithPhoto(
        { url: 'https://your-h5-domain.com/logo.svg' },
        {
          caption: welcomeMessage(session.nickname, session.balance, session.vipLevel),
          parse_mode: 'Markdown',
          ...mainMenuKeyboard(),
        }
      ).catch(async () => {
        // 图片加载失败时回退到纯文本
        await ctx.reply(
          welcomeMessage(session.nickname, session.balance, session.vipLevel),
          { parse_mode: 'Markdown', ...mainMenuKeyboard() }
        );
      });
    } catch (err) {
      console.error('[Start] 错误:', err);
      await ctx.reply(errorMessage('系统繁忙，请稍后重试'));
    }
  });
}
