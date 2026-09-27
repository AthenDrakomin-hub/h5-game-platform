import express from 'express';
import { Telegraf } from 'telegraf';
import { BotContext } from '../middlewares/auth';
import { config } from '../config';
import { formatMoney } from '../utils/format';

/** 通知类型对应的消息模板 */
const notifyTemplates: Record<string, (data: any) => string> = {
  recharge: (data) =>
    `💰 *充值到账通知*\n\n` +
    `充值金额：${formatMoney(data.amount || 0)}\n` +
    `支付方式：${data.payType || '-'}\n` +
    `当前余额：${formatMoney(data.balance || 0)}\n\n` +
    `感谢您的充值，祝您游戏愉快！`,
  withdraw_apply: (data) =>
    `📤 *提现申请已提交*\n\n` +
    `提现金额：${formatMoney(data.amount || 0)}\n` +
    `提现方式：${data.accountType || '-'}\n` +
    `订单号：${data.orderNo || '-'}\n\n` +
    `预计1-2小时内到账，请耐心等待。`,
  withdraw_success: (data) =>
    `✅ *提现到账通知*\n\n` +
    `提现金额：${formatMoney(data.amount || 0)}\n` +
    `到账账户：${data.account || '-'}\n` +
    `当前余额：${formatMoney(data.balance || 0)}\n\n` +
    `提现已成功到账，请注意查收。`,
  win: (data) =>
    `🎉 *中奖通知*\n\n` +
    `游戏：${data.gameName || '-'}\n` +
    `期号：${data.issue || '-'}\n` +
    `中奖金额：${formatMoney(data.amount || 0)}\n` +
    `订单号：${data.orderNo || '-'}\n\n` +
    `恭喜您中奖！奖金已发放至账户余额。`,
  signin_remind: () =>
    `📅 *每日签到提醒*\n\n` +
    `今日签到可领取奖励，连续签到奖励翻倍！\n\n` +
    `点击 /menu → 每日签到 立即领取。`,
  promo: (data) =>
    `🔥 *活动推送*\n\n` +
    `${data.title || '限时活动'}\n\n` +
    `${data.content || ''}\n\n` +
    `点击下方按钮立即参与：`,
  system: (data) =>
    `📢 *系统公告*\n\n` +
    `${data.content || ''}\n\n` +
    `如有疑问请联系客服。`,
};

export class NotifyService {
  private bot: Telegraf<BotContext>;

  constructor(bot: Telegraf<BotContext>) {
    this.bot = bot;
  }

  /** 发送通知给指定用户 */
  async sendToUser(telegramId: number, type: string, data: any = {}): Promise<boolean> {
    try {
      const template = notifyTemplates[type];
      const text = template ? template(data) : data.content || '您有一条新消息';

      const extra: any = { parse_mode: 'Markdown' };

      // 活动推送附带按钮
      if (type === 'promo' && data.actionUrl) {
        extra.reply_markup = {
          inline_keyboard: [[{ text: '🚀 立即参与', url: data.actionUrl }]],
        };
      }

      await this.bot.telegram.sendMessage(telegramId, text, extra);
      return true;
    } catch (err) {
      console.error(`[Notify] 发送失败 telegramId=${telegramId} type=${type}:`, err);
      return false;
    }
  }

  /** 广播通知（管理员使用，需限流） */
  async broadcast(telegramIds: number[], type: string, data: any = {}): Promise<{ success: number; failed: number }> {
    let success = 0;
    let failed = 0;
    for (const id of telegramIds) {
      const ok = await this.sendToUser(id, type, data);
      if (ok) success++;
      else failed++;
      // 限流：每秒最多30条（Telegram 限制）
      await new Promise((r) => setTimeout(r, 35));
    }
    return { success, failed };
  }
}

/** 创建 Express 应用，暴露内部通知接口供后端调用 */
export function createNotifyServer(bot: Telegraf<BotContext>, notifyService: NotifyService): express.Application {
  const app = express();
  app.use(express.json());

  // 鉴权中间件
  app.use('/api/bot/notify', (req, res, next) => {
    const secret = req.headers['x-bot-secret'];
    if (!config.internalSecret || secret !== config.internalSecret) {
      return res.status(401).json({ code: 401, msg: '未授权' });
    }
    next();
  });

  // 单用户通知
  app.post('/api/bot/notify/send', async (req, res) => {
    try {
      const { telegramId, type, data } = req.body;
      if (!telegramId || !type) {
        return res.status(400).json({ code: 400, msg: 'telegramId 和 type 必填' });
      }
      const success = await notifyService.sendToUser(telegramId, type, data);
      res.json({ code: 200, msg: 'success', data: { success } });
    } catch (err: any) {
      res.status(500).json({ code: 500, msg: err.message });
    }
  });

  // 批量广播
  app.post('/api/bot/notify/broadcast', async (req, res) => {
    try {
      const { telegramIds, type, data } = req.body;
      if (!Array.isArray(telegramIds) || telegramIds.length === 0 || !type) {
        return res.status(400).json({ code: 400, msg: 'telegramIds 数组和 type 必填' });
      }
      // 异步执行，立即返回
      res.json({ code: 200, msg: 'broadcast started', data: { total: telegramIds.length } });
      notifyService.broadcast(telegramIds, type, data).then((result) => {
        console.log(`[Notify] 广播完成: 成功${result.success} 失败${result.failed}`);
      });
    } catch (err: any) {
      res.status(500).json({ code: 500, msg: err.message });
    }
  });

  // 健康检查
  app.get('/api/bot/health', (req, res) => {
    res.json({ code: 200, msg: 'ok', data: { status: 'running', bot: config.botUsername, time: Date.now() } });
  });

  return app;
}
