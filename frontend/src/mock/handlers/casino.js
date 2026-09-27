import { http, HttpResponse } from 'msw'

const ok = (data) => HttpResponse.json({ code: 0, data, message: 'success' })

const platforms = [
  { id: 'ag', name: 'AG真人', icon: '🎰', status: 'online', gameCount: 50 },
  { id: 'bbin', name: 'BBIN真人', icon: '🎲', status: 'online', gameCount: 40 },
  { id: 'pragmatic', name: 'Pragmatic', icon: '🎡', status: 'online', gameCount: 200 },
  { id: 'pg', name: 'PG电子', icon: '🎯', status: 'online', gameCount: 150 },
  { id: 'jdb', name: 'JDB电子', icon: '🎪', status: 'online', gameCount: 80 },
  { id: 'km', name: 'KM棋牌', icon: '🀄', status: 'online', gameCount: 30 },
  { id: 'ng', name: 'NG电子', icon: '🎨', status: 'online', gameCount: 100 },
  { id: 'ms', name: 'MG电子', icon: '🎭', status: 'maintenance', gameCount: 120 },
  { id: 'fs', name: '捕鱼王', icon: '🐟', status: 'online', gameCount: 20 },
  { id: 'gsc', name: 'GSC棋牌', icon: '♠️', status: 'online', gameCount: 25 }
]

function genGames(platformId, count = 20) {
  return Array.from({ length: count }, (_, i) => ({
    id: `${platformId}_${i + 1}`,
    name: `${platformId.toUpperCase()}游戏${i + 1}`,
    icon: `https://picsum.photos/seed/${platformId}${i}/120/120`,
    category: i % 3 === 0 ? 'slot' : i % 3 === 1 ? 'table' : 'live',
    isHot: i < 5,
    isNew: i >= count - 3,
    status: 'online'
  }))
}

export const casinoHandlers = [
  http.get('/wap/casino/platforms', () => {
    return ok(platforms)
  }),

  http.get('/wap/casino/games', ({ request }) => {
    const url = new URL(request.url)
    const platformId = url.searchParams.get('platformId') || 'ag'
    return ok(genGames(platformId, 24))
  }),

  http.get('/wap/casino/game/:id/url', ({ params }) => {
    return ok({
      gameUrl: `https://example.com/games/${params.id}?token=mock`,
      method: 'iframe',
      title: `游戏 ${params.id}`
    })
  }),

  http.get('/wap/casino/hot-games', () => {
    const games = platforms.slice(0, 4).flatMap(p => genGames(p.id, 3))
    return ok(games.slice(0, 12))
  }),

  http.get('/wap/casino/new-games', () => {
    const games = platforms.slice(0, 3).flatMap(p => genGames(p.id, 4))
    return ok(games.slice(0, 12))
  }),

  http.get('/wap/casino/favorites', () => {
    return ok(genGames('ag', 6))
  }),

  http.post('/wap/casino/favorite/:id', ({ params }) => {
    return ok({ gameId: params.id, isFavorite: true })
  })
]
