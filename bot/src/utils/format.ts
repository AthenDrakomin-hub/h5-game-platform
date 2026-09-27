/** 金额格式化 */
export function formatMoney(amount: number): string {
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

/** VIP 等级显示 */
export function formatVip(level: number): string {
  if (level <= 0) return '普通用户';
  return `VIP${level}`;
}

/** 时间格式化 */
export function formatTime(dateStr: string): string {
  const d = new Date(dateStr);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** 隐藏手机号中间四位 */
export function maskPhone(phone: string): string {
  if (phone.length < 7) return phone;
  return phone.slice(0, 3) + '****' + phone.slice(-4);
}

/** 生成欢迎消息 */
export function welcomeMessage(nickname: string, balance: number, vipLevel: number): string {
  return `🎮 *NOVA 新星娱乐*

欢迎您，${nickname}！

💰 账户余额：${formatMoney(balance)}
👑 会员等级：${formatVip(vipLevel)}

安全 · 公平 · 透明，点击下方按钮立即开始游戏！`;
}

/** 主菜单消息 */
export function mainMenuMessage(nickname: string, balance: number, vipLevel: number): string {
  return `📋 *主菜单*

👤 ${nickname}
💰 余额：${formatMoney(balance)}
👑 ${formatVip(vipLevel)}

请选择您要进行的操作：`;
}

/** 错误消息 */
export function errorMessage(msg: string): string {
  return `❌ *操作失败*\n\n${msg}\n\n请稍后重试或联系客服。`;
}
