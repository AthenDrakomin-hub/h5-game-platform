<template>
  <div v-if="show" class="puzzle-captcha">
    <div class="captcha-header">
      <span class="captcha-title">安全验证</span>
      <van-icon name="cross" class="captcha-close" @click="$emit('close')" />
    </div>
    <div class="captcha-canvas-wrap">
      <canvas ref="bgCanvas" :width="canvasWidth" :height="canvasHeight" class="captcha-bg" />
      <canvas
        ref="blockCanvas"
        :width="blockSize"
        :height="canvasHeight"
        class="captcha-block"
        :style="{ transform: `translateX(${sliderX}px)` }"
      />
      <div v-if="loading" class="captcha-loading">
        <van-loading color="#f0d080" />加载中...
      </div>
      <div v-if="infoText" class="captcha-info" :class="{ fail: infoFail }">
        {{ infoText }}
      </div>
    </div>
    <div class="captcha-slider-track">
      <div class="captcha-slider-bg">{{ sliderText }}</div>
      <div
        class="captcha-slider-btn"
        :class="{ dragging: isDragging }"
        :style="{ transform: `translateX(${sliderX}px)` }"
        @mousedown="startDrag"
        @touchstart.passive="startDrag"
      >
        <van-icon name="arrow" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  canvasWidth: { type: Number, default: 300 },
  canvasHeight: { type: Number, default: 150 },
  blockSize: { type: Number, default: 45 },
  tolerance: { type: Number, default: 8 },
  sliderText: { type: String, default: '拖动滑块完成拼图' },
  successText: { type: String, default: '验证通过' },
  failText: { type: String, default: '验证失败，请重试' }
})

const emit = defineEmits(['success', 'fail', 'close'])

const bgCanvas = ref(null)
const blockCanvas = ref(null)
const sliderX = ref(0)
const targetX = ref(0)
const targetY = ref(0)
const isDragging = ref(false)
const loading = ref(false)
const infoText = ref('')
const infoFail = ref(false)
let startX = 0

function drawBackground() {
  const ctx = bgCanvas.value.getContext('2d')
  // 渐变背景
  const gradient = ctx.createLinearGradient(0, 0, props.canvasWidth, props.canvasHeight)
  gradient.addColorStop(0, '#2a1f10')
  gradient.addColorStop(0.5, '#1a130a')
  gradient.addColorStop(1, '#16213e')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, props.canvasWidth, props.canvasHeight)

  // 随机几何图形
  for (let i = 0; i < 8; i++) {
    ctx.fillStyle = `rgba(${Math.floor(Math.random() * 100 + 100)}, ${Math.floor(Math.random() * 80 + 60)}, ${Math.floor(Math.random() * 50 + 30)}, 0.3)`
    ctx.beginPath()
    const x = Math.random() * props.canvasWidth
    const y = Math.random() * props.canvasHeight
    const r = Math.random() * 30 + 10
    if (i % 2 === 0) {
      ctx.arc(x, y, r, 0, Math.PI * 2)
    } else {
      ctx.rect(x, y, r * 1.5, r)
    }
    ctx.fill()
  }

  // 文字水印
  ctx.fillStyle = 'rgba(240, 208, 128, 0.15)'
  ctx.font = 'bold 28px Arial'
  ctx.fillText('SECURE', 30, props.canvasHeight / 2)
}

function drawBlock() {
  const ctx = blockCanvas.value.getContext('2d')
  const bgCtx = bgCanvas.value.getContext('2d')

  // 目标位置
  targetX.value = Math.floor(Math.random() * (props.canvasWidth - props.blockSize - 30)) + 20
  targetY.value = Math.floor(Math.random() * (props.canvasHeight - props.blockSize - 20)) + 10

  // 在背景上挖空
  bgCtx.save()
  bgCtx.fillStyle = 'rgba(0, 0, 0, 0.6)'
  bgCtx.strokeStyle = 'rgba(255, 255, 255, 0.8)'
  bgCtx.lineWidth = 1
  drawPuzzleShape(bgCtx, targetX.value, targetY.value, props.blockSize)
  bgCtx.fill()
  bgCtx.stroke()
  bgCtx.restore()

  // 绘制滑块块
  const imageData = bgCtx.getImageData(targetX.value, targetY.value, props.blockSize, props.blockSize)
  ctx.putImageData(imageData, 0, targetY.value)

  // 滑块边框
  ctx.save()
  ctx.strokeStyle = 'rgba(240, 208, 128, 0.9)'
  ctx.lineWidth = 2
  drawPuzzleShape(ctx, 0, targetY.value, props.blockSize)
  ctx.stroke()
  ctx.restore()
}

function drawPuzzleShape(ctx, x, y, size) {
  const r = size * 0.2
  ctx.beginPath()
  ctx.moveTo(x, y)
  ctx.lineTo(x + size / 2 - r, y)
  ctx.arc(x + size / 2, y, r, Math.PI, 0, true)
  ctx.lineTo(x + size, y)
  ctx.lineTo(x + size, y + size / 2 - r)
  ctx.arc(x + size, y + size / 2, r, -Math.PI / 2, Math.PI / 2, false)
  ctx.lineTo(x + size, y + size)
  ctx.lineTo(x, y + size)
  ctx.closePath()
}

function startDrag(e) {
  if (loading.value) return
  isDragging.value = true
  startX = (e.touches ? e.touches[0].clientX : e.clientX) - sliderX.value
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag, { passive: false })
  document.addEventListener('touchend', endDrag)
}

function onDrag(e) {
  if (!isDragging.value) return
  e.preventDefault?.()
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  sliderX.value = Math.max(0, Math.min(props.canvasWidth - props.blockSize, clientX - startX))
}

function endDrag() {
  if (!isDragging.value) return
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)

  const diff = Math.abs(sliderX.value - targetX.value)
  if (diff <= props.tolerance) {
    infoText.value = props.successText
    infoFail.value = false
    setTimeout(() => {
      emit('success', { deviation: diff })
      reset()
    }, 600)
  } else {
    infoText.value = props.failText
    infoFail.value = true
    setTimeout(() => {
      emit('fail', { deviation: diff })
      reset()
      init()
    }, 800)
  }
}

function reset() {
  sliderX.value = 0
  infoText.value = ''
  infoFail.value = false
}

function init() {
  loading.value = true
  setTimeout(() => {
    drawBackground()
    drawBlock()
    loading.value = false
  }, 200)
}

watch(() => props.show, (val) => {
  if (val) {
    reset()
    init()
  }
})

onMounted(() => {
  if (props.show) init()
})
</script>

<style scoped>
.puzzle-captcha {
  padding: 16px;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
}
.captcha-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.captcha-title {
  font-size: 16px;
  font-weight: 600;
  color: #f2e0b8;
}
.captcha-close {
  font-size: 18px;
  color: #8a7a5a;
  cursor: pointer;
}
.captcha-canvas-wrap {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}
.captcha-bg {
  display: block;
  width: 100%;
  height: auto;
}
.captcha-block {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}
.captcha-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: #f0d080;
  font-size: 13px;
  gap: 8px;
}
.captcha-info {
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 12px;
  background: rgba(7, 193, 96, 0.9);
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
}
.captcha-info.fail {
  background: rgba(238, 10, 36, 0.9);
}
.captcha-slider-track {
  position: relative;
  margin-top: 12px;
  height: 40px;
  background: rgba(255,255,255,0.06);
  border-radius: 10px;
  overflow: hidden;
}
.captcha-slider-bg {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6a5a40;
  font-size: 13px;
}
.captcha-slider-btn {
  position: absolute;
  top: 0;
  left: 0;
  width: 40px;
  height: 40px;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3a2610;
  cursor: grab;
  box-shadow: 0 2px 8px rgba(240, 208, 128, 0.4);
  user-select: none;
}
.captcha-slider-btn.dragging {
  cursor: grabbing;
  transform: scale(1.05);
}
</style>
