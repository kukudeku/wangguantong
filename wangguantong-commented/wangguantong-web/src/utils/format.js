/**
 * 把后端 LocalDateTime 常见的 2026-07-08T21:15:11 格式转成适合页面阅读的格式。
 * 没有时间时显示短横线；slice(0, 19) 去掉秒后面可能存在的小数部分。
 */
export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}
