import { Telegraf } from 'telegraf';
import express from 'express';
import { config, validateConfig } from './config';
import { BotContext, authMiddleware } from './middlewares/auth';
import { registerStartHandler } from './handlers/start';
import { registerMenuHandler } from './handlers/menu';
import { registerWalletHandler } from './handlers/wallet';
import { registerGameHandler } from './handlers/game';
import { registerPromoHandler } from './handlers/promo';
import { registerSupportHandler } from './handlers/support';
import { NotifyService, createNotifyServer } from './handlers/notify';

async function main() {
  // 配置校验
  const errors = validateConfig();
  if (errors.length > 0) {
    console.error('❌ 配置错误：');
    errors.forEach((e) => console.error(`  - ${e}`));
    process.exit(1);
  }

  // 创建 Bot 实例
  const bot = new Telegraf<BotContext>(config.botToken);

  // 全局中间件
  bot.use(authMiddleware);

  // 注册处理器
  registerStartHandler(bot);
  registerMenuHandler(bot);
  registerWalletHandler(bot);
  registerGameHandler(bot);
  registerPromoHandler(bot);
  registerSupportHandler(bot);

  // 通知服务
  const notifyService = new NotifyService(bot);

  // 错误处理
  bot.catch((err, ctx) => {
    console.error('[Bot] 未捕获错误:', err);
    ctx.reply('⚠️ 系统繁忙，请稍后重试或点击 /menu 返回主菜单。').catch(() => {});
  });

  if (config.mode === 'webhook') {
    // ===== Webhook 模式 =====
    const app = express();
    app.use(express.json());

    // Telegram Webhook 端点
    app.post('/bot/webhook', (req, res) => {
      bot.handleUpdate(req.body);
      res.sendStatus(200);
    });

    // 内部通知 API
    const notifyApp = createNotifyServer(bot, notifyService);
    app.use(notifyApp);

    app.listen(config.webhook.port, () => {
      console.log(`✅ Bot Webhook 服务已启动: http://0.0.0.0:${config.webhook.port}`);
      console.log(`   Webhook URL: ${config.webhook.url}`);
      console.log(`   通知 API: http://0.0.0.0:${config.webhook.port}/api/bot/notify`);
    });

    // 设置 Webhook
    try {
      await bot.telegram.setWebhook(config.webhook.url, {
        secret_token: config.webhook.secret,
        drop_pending_updates: true,
      });
      console.log('✅ Webhook 已设置');
    } catch (err) {
      console.error('❌ Webhook 设置失败:', err);
    }
  } else {
    // ===== Long Polling 模式（开发/调试用） =====
    // 同时启动内部通知 API（端口 +1）
    const notifyApp = createNotifyServer(bot, notifyService);
    const notifyPort = config.webhook.port + 1;
    notifyApp.listen(notifyPort, () => {
      console.log(`✅ 通知 API 已启动: http://0.0.0.0:${notifyPort}/api/bot/notify`);
    });

    bot.launch({
      dropPendingUpdates: true,
    }).then(() => {
      console.log(`✅ Bot ${config.botUsername} 已启动（Long Polling 模式）`);
      console.log(`   通知 API 端口: ${notifyPort}`);
    }).catch((err) => {
      console.error('❌ Bot 启动失败:', err);
      process.exit(1);
    });
  }

  // 优雅退出
  process.once('SIGINT', () => {
    console.log('\n收到 SIGINT，正在关闭...');
    bot.stop('SIGINT');
    process.exit(0);
  });
  process.once('SIGTERM', () => {
    console.log('\n收到 SIGTERM，正在关闭...');
    bot.stop('SIGTERM');
    process.exit(0);
  });
}

main().catch((err) => {
  console.error('❌ 启动失败:', err);
  process.exit(1);
});
