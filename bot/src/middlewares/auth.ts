import { Context } from 'telegraf';
import { userService, BotUserSession } from '../services/user';

/** 扩展 Context 类型 */
export interface BotContext extends Context {
  session?: BotUserSession;
}

/** 鉴权中间件：自动注册/登录并挂载 session */
export async function authMiddleware(ctx: BotContext, next: () => Promise<void>) {
  const tgUser = ctx.from;
  if (!tgUser) {
    await next();
    return;
  }

  try {
    // 解析邀请码（/start invite_xxx）
    let inviteCode: string | undefined;
    if (ctx.message && 'text' in ctx.message) {
      const text = ctx.message.text;
      if (text.startsWith('/start ') && text.length > 7) {
        const param = text.slice(7).trim();
        if (param.startsWith('invite_')) {
          inviteCode = param.slice(7);
        }
      }
    }

    const session = await userService.getOrCreateUser(
      tgUser.id,
      {
        username: tgUser.username,
        firstName: tgUser.first_name,
        lastName: tgUser.last_name,
        languageCode: tgUser.language_code,
      },
      inviteCode
    );
    ctx.session = session;
  } catch (err) {
    console.error('[Auth] 自动注册/登录失败:', err);
    // 鉴权失败不阻断，session 为 undefined，handler 需自行判断
  }

  await next();
}

/** 要求已登录的守卫 */
export function requireAuth(ctx: BotContext): boolean {
  if (!ctx.session) {
    ctx.reply('⚠️ 账号登录中，请稍候再试，或点击 /start 重新进入。');
    return false;
  }
  return true;
}

/** 管理员守卫 */
export function requireAdmin(ctx: BotContext, adminIds: number[]): boolean {
  if (!ctx.from || !adminIds.includes(ctx.from.id)) {
    return false;
  }
  return true;
}
