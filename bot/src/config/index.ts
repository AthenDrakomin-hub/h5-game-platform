import dotenv from 'dotenv';

dotenv.config();

export const config = {
  botToken: process.env.BOT_TOKEN || '',
  botUsername: process.env.BOT_USERNAME || '@NOVA_Lucky_Bot',
  mode: (process.env.BOT_MODE as 'webhook' | 'polling') || 'polling',
  webhook: {
    url: process.env.WEBHOOK_URL || '',
    port: parseInt(process.env.WEBHOOK_PORT || '3001', 10),
    secret: process.env.WEBHOOK_SECRET || '',
  },
  miniAppUrl: process.env.MINI_APP_URL || 'https://your-h5-domain.com',
  logoUrl: process.env.LOGO_URL || (process.env.MINI_APP_URL || 'https://your-h5-domain.com') + '/logo.svg',
  apiBaseUrl: process.env.API_BASE_URL || 'http://127.0.0.1:8888/api',
  internalSecret: process.env.BOT_INTERNAL_SECRET || '',
  redisUrl: process.env.REDIS_URL || '',  // Redis 连接URL（配置后会话存储用Redis，否则用内存）
  adminIds: (process.env.ADMIN_TELEGRAM_IDS || '')
    .split(',')
    .map((id) => parseInt(id.trim(), 10))
    .filter((id) => !isNaN(id)),
  logLevel: process.env.LOG_LEVEL || 'info',
};

export function validateConfig(): string[] {
  const errors: string[] = [];
  if (!config.botToken) errors.push('BOT_TOKEN 未配置');
  if (config.mode === 'webhook' && !config.webhook.url) {
    errors.push('WEBHOOK_URL 未配置（webhook 模式下必填）');
  }
  return errors;
}
