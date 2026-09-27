import { Telegraf } from 'telegraf';
import { BotContext } from '../middlewares/auth';
import { supportKeyboard, backKeyboard } from '../keyboards/main';

const faqAnswers: Record<string, string> = {
  faq_recharge:
    '💰 *充值常见问题*\n\n' +
    '*Q: 充值多久到账？*\nA: 在线支付即时到账，银行卡转账1-30分钟内到账。\n\n' +
    '*Q: 最低充值金额？*\nA: 最低充值100元。\n\n' +
    '*Q: 充值未到账怎么办？*\nA: 请保留支付凭证，联系在线客服核实处理。\n\n' +
    '*Q: 支持哪些支付方式？*\nA: 支持银行卡转账、USDT(TRC20)、在线支付。',
  faq_withdraw:
    '📤 *提现常见问题*\n\n' +
    '*Q: 提现多久到账？*\nA: 工作日1-2小时内处理，非工作日可能延迟。\n\n' +
    '*Q: 最低提现金额？*\nA: 最低提现100元。\n\n' +
    '*Q: 提现手续费？*\nA: 普通用户收取1%手续费，VIP2及以上免手续费。\n\n' +
    '*Q: 提现被驳回怎么办？*\nA: 请检查账户信息是否正确，或联系客服核实。',
  faq_game:
    '🎮 *游戏常见问题*\n\n' +
    '*Q: 游戏结果以什么为准？*\nA: 以系统官方开奖结果为准。\n\n' +
    '*Q: 开奖时间？*\nA: 极速赛车/幸运飞艇每5分钟一期，时时彩高频开奖。\n\n' +
    '*Q: 投注后可以撤销吗？*\nA: 开奖前可撤销未结算订单，开奖后不可撤销。\n\n' +
    '*Q: 游戏公平吗？*\nA: 所有游戏采用随机数生成器，结果公开透明。',
  faq_account:
    '👤 *账号常见问题*\n\n' +
    '*Q: 如何修改密码？*\nA: 进入游戏内「我的-设置-修改密码」。\n\n' +
    '*Q: 忘记密码怎么办？*\nA: 通过绑定手机号找回，或联系客服核实身份后重置。\n\n' +
    '*Q: 可以绑定多个手机号吗？*\nA: 一个账号只能绑定一个手机号。\n\n' +
    '*Q: 如何注销账号？*\nA: 请联系在线客服提交注销申请。',
};

export function registerSupportHandler(bot: Telegraf<BotContext>) {
  // /support 命令
  bot.command('support', async (ctx) => {
    await ctx.reply('💬 *在线客服*\n\n请选择问题类型：', {
      parse_mode: 'Markdown',
      ...supportKeyboard(),
    });
  });

  // FAQ 自动回复
  Object.entries(faqAnswers).forEach(([key, answer]) => {
    bot.action(key, async (ctx) => {
      await ctx.editMessageText(answer, {
        parse_mode: 'Markdown',
        ...backKeyboard('support'),
      });
      await ctx.answerCbQuery();
    });
  });

  // 转人工客服
  bot.action('support_human', async (ctx) => {
    await ctx.editMessageText(
      '🧑‍💼 *人工客服*\n\n' +
      '正在为您转接人工客服，请稍候...\n\n' +
      '客服工作时间：每日 09:00 - 24:00\n\n' +
      '您也可以通过以下方式联系我们：\n' +
      '📧 邮箱：support@nova-game.com\n' +
      '💬 Telegram：@NOVA_Support',
      {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [
            [{ text: '📩 联系客服', url: 'https://t.me/NOVA_Support' }],
            [{ text: '🔙 返回客服菜单', callback_data: 'support' }],
          ],
        },
      }
    );
    await ctx.answerCbQuery();
  });

  // 处理用户直接发送的消息（简单智能回复）
  bot.on('text', async (ctx, next) => {
    const text = ctx.message.text.toLowerCase();
    // 忽略命令
    if (text.startsWith('/')) return next();

    // 关键词匹配
    if (text.includes('充值') || text.includes('存款') || text.includes('recharge')) {
      await ctx.reply(faqAnswers.faq_recharge, { parse_mode: 'Markdown', ...supportKeyboard() });
    } else if (text.includes('提现') || text.includes('取款') || text.includes('withdraw')) {
      await ctx.reply(faqAnswers.faq_withdraw, { parse_mode: 'Markdown', ...supportKeyboard() });
    } else if (text.includes('游戏') || text.includes('开奖') || text.includes('game')) {
      await ctx.reply(faqAnswers.faq_game, { parse_mode: 'Markdown', ...supportKeyboard() });
    } else if (text.includes('账号') || text.includes('密码') || text.includes('注册') || text.includes('登录')) {
      await ctx.reply(faqAnswers.faq_account, { parse_mode: 'Markdown', ...supportKeyboard() });
    } else if (text.includes('客服') || text.includes('人工') || text.includes('help')) {
      await ctx.reply('💬 *在线客服*\n\n请选择问题类型：', { parse_mode: 'Markdown', ...supportKeyboard() });
    } else if (text.includes('你好') || text.includes('hi') || text.includes('hello') || text.includes('在吗')) {
      await ctx.reply('👋 您好！欢迎来到 NOVA 新星娱乐客服中心。\n\n请问有什么可以帮您？您可以点击下方按钮选择问题类型，或直接描述您的问题。', {
        parse_mode: 'Markdown',
        ...supportKeyboard(),
      });
    } else {
      // 未识别的消息，引导使用菜单
      await ctx.reply('🤖 我是智能客服助手，您可以通过以下方式获得帮助：\n\n• 点击 /menu 打开主菜单\n• 点击 /support 联系客服\n• 直接输入关键词（如"充值""提现""游戏"）\n\n如需人工客服，请点击下方按钮。', {
        parse_mode: 'Markdown',
        ...supportKeyboard(),
      });
    }
  });
}
