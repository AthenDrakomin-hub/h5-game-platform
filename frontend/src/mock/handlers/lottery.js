import { http, HttpResponse } from 'msw'

const ok = (data) => HttpResponse.json({ code: 0, data, message: 'success' })

// 生成随机开奖号码
function genPk10Numbers() {
  const nums = []
  const pool = [1,2,3,4,5,6,7,8,9,10]
  for (let i = 0; i < 10; i++) {
    const idx = Math.floor(Math.random() * pool.length)
    nums.push(pool.splice(idx, 1)[0])
  }
  return nums
}

function genSscNumbers() {
  return Array.from({ length: 5 }, () => Math.floor(Math.random() * 10))
}

function genPc28Numbers() {
  return Array.from({ length: 3 }, () => Math.floor(Math.random() * 10))
}

function genLhcNumbers() {
  const red = []
  const pool = Array.from({ length: 49 }, (_, i) => i + 1)
  for (let i = 0; i < 6; i++) {
    const idx = Math.floor(Math.random() * pool.length)
    red.push(pool.splice(idx, 1)[0])
  }
  return { red: red.sort((a, b) => a - b), blue: Math.floor(Math.random() * 49) + 1 }
}

const lotteryGames = {
  pk10: [
    { code: 'bjpk10', name: '北京PK10', icon: '🏎️', status: 'open' },
    { code: 'jndpk10', name: '加拿大PK10', icon: '🏎️', status: 'open' },
    { code: 'ffpk10', name: '飞飞PK10', icon: '🏎️', status: 'open' }
  ],
  lhc: [
    { code: 'hk6', name: '香港六合彩', icon: '🎱', status: 'open' },
    { code: 'jndlhc', name: '加拿大六合彩', icon: '🎱', status: 'open' }
  ],
  ssc: [
    { code: 'cqssc', name: '重庆时时彩', icon: '🎰', status: 'open' },
    { code: 'tjssc', name: '天津时时彩', icon: '🎰', status: 'open' },
    { code: 'xjssc', name: '新疆时时彩', icon: '🎰', status: 'open' }
  ],
  pc28: [
    { code: 'pc28', name: '加拿大28', icon: '🎲', status: 'open' },
    { code: 'xy28', name: '幸运28', icon: '🎲', status: 'open' }
  ]
}

export const lotteryHandlers = [
  http.get('/wap/lottery/categories', () => {
    return ok([
      { id: 'pk10', name: 'PK10', icon: '🏎️' },
      { id: 'lhc', name: '六合彩', icon: '🎱' },
      { id: 'ssc', name: '时时彩', icon: '🎰' },
      { id: 'pc28', name: '28彩', icon: '🎲' }
    ])
  }),

  http.get('/wap/lottery/games', ({ request }) => {
    const url = new URL(request.url)
    const category = url.searchParams.get('category')
    const games = category ? (lotteryGames[category] || []) : Object.values(lotteryGames).flat()
    return ok(games)
  }),

  http.get('/wap/lottery/game/:code', ({ params }) => {
    const allGames = Object.values(lotteryGames).flat()
    const game = allGames.find(g => g.code === params.code)
    return ok(game || { code: params.code, name: params.code, status: 'open' })
  }),

  http.get('/wap/lottery/:code/current-issue', ({ params }) => {
    const now = new Date()
    const issue = now.getFullYear().toString() +
      String(now.getMonth() + 1).padStart(2, '0') +
      String(now.getDate()).padStart(2, '0') +
      '-' + String(Math.floor(Math.random() * 100)).padStart(2, '0')
    const closeTime = new Date(now.getTime() + 3 * 60 * 1000).toISOString()
    return ok({
      issue,
      closeTime,
      countDown: 180,
      status: 'selling'
    })
  }),

  http.get('/wap/lottery/:code/history', ({ params, request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '30')
    const code = params.code
    const category = code.includes('pk10') ? 'pk10' : code.includes('lhc') ? 'lhc' : code.includes('ssc') ? 'ssc' : 'pc28'

    const list = Array.from({ length: pageSize }, (_, i) => {
      const issueNum = (page - 1) * pageSize + i
      let numbers
      if (category === 'pk10') numbers = genPk10Numbers()
      else if (category === 'ssc') numbers = genSscNumbers()
      else if (category === 'pc28') numbers = genPc28Numbers()
      else numbers = genLhcNumbers()

      return {
        id: issueNum,
        issue: `20260927-${String(100 - issueNum).padStart(2, '0')}`,
        numbers,
        openTime: new Date(Date.now() - i * 3 * 60 * 1000).toISOString()
      }
    })

    return ok({ list, total: 1000, page, pageSize })
  }),

  http.get('/wap/lottery/:code/trend', ({ params }) => {
    const code = params.code
    const category = code.includes('pk10') ? 'pk10' : code.includes('lhc') ? 'lhc' : code.includes('ssc') ? 'ssc' : 'pc28'
    const periods = Array.from({ length: 30 }, (_, i) => {
      let numbers
      if (category === 'pk10') numbers = genPk10Numbers()
      else if (category === 'ssc') numbers = genSscNumbers()
      else if (category === 'pc28') numbers = genPc28Numbers()
      else numbers = genLhcNumbers()
      return { issue: `期号${30 - i}`, numbers }
    })
    return ok({ periods, category })
  }),

  http.get('/wap/lottery/:code/play-config', ({ params }) => {
    const code = params.code
    const category = code.includes('pk10') ? 'pk10' : code.includes('lhc') ? 'lhc' : code.includes('ssc') ? 'ssc' : 'pc28'

    const configs = {
      pk10: {
        playTypes: [
          { id: 'champion', name: '冠军', odds: 9.8 },
          { id: 'runnerup', name: '亚军', odds: 9.8 },
          { id: 'third', name: '第三名', odds: 9.8 },
          { id: 'bigsmall', name: '大小', odds: 1.98 },
          { id: 'oddeven', name: '单双', odds: 1.98 }
        ],
        minBet: 1,
        maxBet: 100000
      },
      ssc: {
        playTypes: [
          { id: 'wan', name: '万位', odds: 9.8 },
          { id: 'qian', name: '千位', odds: 9.8 },
          { id: 'bai', name: '百位', odds: 9.8 },
          { id: 'shi', name: '十位', odds: 9.8 },
          { id: 'ge', name: '个位', odds: 9.8 },
          { id: 'bigsmall', name: '大小', odds: 1.98 }
        ],
        minBet: 1,
        maxBet: 100000
      },
      pc28: {
        playTypes: [
          { id: 'sum', name: '和值', odds: '浮动' },
          { id: 'bigsmall', name: '大小', odds: 1.98 },
          { id: 'oddeven', name: '单双', odds: 1.98 },
          { id: 'combination', name: '组合', odds: 3.8 }
        ],
        minBet: 1,
        maxBet: 100000
      },
      lhc: {
        playTypes: [
          { id: 'special', name: '特码', odds: 47 },
          { id: 'red', name: '红波', odds: 2.8 },
          { id: 'blue', name: '蓝波', odds: 2.8 },
          { id: 'green', name: '绿波', odds: 2.8 },
          { id: 'zodiac', name: '生肖', odds: 12 }
        ],
        minBet: 1,
        maxBet: 100000
      }
    }

    return ok(configs[category] || configs.pk10)
  }),

  http.post('/wap/lottery/bet', async ({ request }) => {
    const body = await request.json()
    const betId = 'BET' + Date.now()
    return ok({
      betId,
      status: 'success',
      amount: body.amount || 10,
      issue: body.issue,
      createdAt: new Date().toISOString()
    })
  }),

  http.get('/wap/lottery/bet-records', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      betId: 'BET' + (Date.now() - i * 1000),
      gameCode: 'bjpk10',
      gameName: '北京PK10',
      issue: '20260927-01',
      playType: '冠军',
      betContent: '5号',
      amount: 10 + i,
      odds: 9.8,
      status: i % 3 === 0 ? 'won' : i % 3 === 1 ? 'lost' : 'pending',
      winAmount: i % 3 === 0 ? 98 : 0,
      createdAt: new Date(Date.now() - i * 3 * 60 * 1000).toISOString()
    }))
    return ok({ list, total: 100, page, pageSize })
  })
]
