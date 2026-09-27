import { Telegraf } from 'telegraf';
import { BotContext } from '../middlewares/auth';
import { config } from '../config';
import { backKeyboard } from '../keyboards/main';

const gameInfo: Record<string, { name: string; icon: string; desc: string }> = {
  game_racing: { name: '极速赛车', icon: '🏎️', desc: '每5分钟一期，猜冠军车号，赔率高达1:9' },
  game_airship: { name: '幸运飞艇', icon: '✈️', desc: '每5分钟一期，10艘飞艇竞速，多种玩法' },
  game_lottery: { name: '时时彩', icon: '🎯', desc: '高频开奖，大小单双、五星定位等玩法' },
  game_casino: { name: '娱乐城', icon: '🎰', desc: '真人视讯、老虎机、棋牌等多种游戏' },
};

export function registerGameHandler(bot: Telegraf<BotContext>) {
  Object.entries(gameInfo).forEach(([key, info]) => {
    bot.action(key, async (ctx) => {
      await ctx.editMessageText(
        `${info.icon} *${info.name}*\n\n${info.desc}\n\n点击下方按钮立即进入游戏：`,
        {
          parse_mode: 'Markdown',
          reply_markup: {
            inline_keyboard: [
              [{ text: `🚀 进入${info.name}`, web_app: { url: config.miniAppUrl } }],
              [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
            ],
          },
        }
      );
      await ctx.answerCbQuery();
    });
  });

  // 开奖结果查询（占位，实际需对接后端开奖接口）
  bot.action('game_result', async (ctx) => {
    await ctx.editMessageText(
      '📊 *开奖结果*\n\n请进入游戏内查看最新开奖结果和历史走势。',
      {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [
            [{ text: '🚀 查看开奖', web_app: { url: config.miniAppUrl } }],
            [{ text: '🔙 返回', callback_data: 'main_menu' }],
          ],
        },
      }
    );
    await ctx.answerCbQuery();
  });
}
