<template>
  <div class="camera-window">
    <canvas
      v-show="hasFrame"
      ref="previewCanvas"
      class="live-stream"
      :style="{ transform: `rotate(${rotationAngle}deg)` }"
    />
    <img
      ref="streamImg"
      class="hidden-stream-img"
      @load="onStreamLoad"
      @error="onStreamError"
      alt=""
    />
    <div v-if="!hasFrame" class="camera-placeholder">
      <div v-if="errorType === 'connect_fail'">
        <i class="el-icon-camera" style="color: #F56C6C;"></i>
        <p class="text-danger" style="margin-top:8px;">未监测到设备</p>
      </div>
      <div v-else-if="errorType === 'occupied'">
        <i class="el-icon-warning" style="color: #E6A23C;"></i>
        <p class="text-warning" style="margin-top:8px;">镜头被其他程序占用</p>
        <el-button type="text" size="mini" @click="retry">重试连接</el-button>
      </div>
      <div v-else>
        <i class="el-icon-loading"></i>
        <p style="margin-top:8px;">正在初始化高拍仪引擎...</p>
      </div>
    </div>
    <div v-if="reconnecting && hasFrame" class="reconnect-tip">
      <i class="el-icon-loading"></i>
      <span>信号波动，正在重连…</span>
    </div>
    <div v-if="errorType === 'occupied' && hasFrame" class="occupied-banner">
      <i class="el-icon-warning"></i>
      <span>镜头被其他程序占用</span>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CameraPreviewPanel',
  props: {
    serviceUrl: { type: String, default: 'http://127.0.0.1:38088' },
    rotationAngle: { type: Number, default: 0 }
  },
  data() {
    return {
      hasFrame: false,
      reconnecting: false,
      errorType: '',
      isPolling: false,
      initPollCount: 0,
      initPollTimer: null,
      _renderTimer: null,
      _heartbeatTimer: null,
      _retryTimer: null,
      streamToken: 0,
      _reconnectAttempt: 0,
      _lastFrameTime: 0
    }
  },
  beforeDestroy() {
    this._teardown()
  },
  methods: {
    start() {
      if (this._renderTimer) return
      this.streamToken++
      this.hasFrame = false
      this.reconnecting = false
      this.errorType = ''
      this._reconnectAttempt = 0
      this._lastFrameTime = Date.now()
      this._initStatus()
      this._heartbeatTimer = setInterval(this._heartbeatCheck, 2000)
    },

    stop() {
      this._teardown()
    },

    _teardown() {
      this.streamToken++
      if (this._renderTimer) { clearInterval(this._renderTimer); this._renderTimer = null }
      if (this._heartbeatTimer) { clearInterval(this._heartbeatTimer); this._heartbeatTimer = null }
      if (this._retryTimer) { clearTimeout(this._retryTimer); this._retryTimer = null }
      if (this.initPollTimer) { clearTimeout(this.initPollTimer); this.initPollTimer = null }
      this.isPolling = false
      this.initPollCount = 0
      const img = this.$refs.streamImg
      if (img) img.src = ''
      this.hasFrame = false
      this.reconnecting = false
      this.errorType = ''
    },

    markOccupied() { this.errorType = 'occupied' },

    markConnectFail() {
      this.errorType = 'connect_fail'
      const img = this.$refs.streamImg
      if (img) img.src = ''
    },

    clearError() {
      if (this.errorType === 'connect_fail') {
        this.start()
      } else {
        this.errorType = ''
      }
    },

    isReady() { return this.hasFrame && this.errorType !== 'occupied' },

    retry() {
      this.streamToken++
      if (this.initPollTimer) { clearTimeout(this.initPollTimer); this.initPollTimer = null }
      this.isPolling = false
      this.initPollCount = 0
      this.errorType = ''
      this.hasFrame = false
      this._reconnectAttempt = 0
      this._initStatus()
    },

    async _initStatus() {
      const token = this.streamToken
      try {
        await axios.get(`${this.serviceUrl}/video=status?t=${Date.now()}`, { timeout: 5000 })
        if (token !== this.streamToken) return
        this.isPolling = false
        this.initPollCount = 0
        if (this.initPollTimer) { clearTimeout(this.initPollTimer); this.initPollTimer = null }
        this._attachStream()
      } catch (e) {
        if (token !== this.streamToken) return
        console.warn('[CameraPreviewPanel] /video=status 探测失败', {
          serviceUrl: this.serviceUrl,
          message: e && e.message,
          code: e && e.code
        })
        this._scheduleInitRetry()
      }
    },

    _scheduleInitRetry() {
      if (this.streamToken === 0) return
      const MAX = 20
      if (this.initPollCount >= MAX) {
        this.isPolling = false
        this.initPollCount = 0
        this.errorType = 'connect_fail'
        this.$emit('launch-failed')
        return
      }
      this.isPolling = true
      this.initPollCount++
      if (this.initPollTimer) clearTimeout(this.initPollTimer)
      this.initPollTimer = setTimeout(() => {
        if (this.streamToken === 0) return
        this._initStatus()
      }, 3000)
    },

    _attachStream() {
      const token = this.streamToken
      const url = `${this.serviceUrl}/video=stream&camidx=0&t=${Date.now()}_${token}_${this._reconnectAttempt}`
      this._lastFrameTime = Date.now()
      this.$nextTick(() => {
        if (token !== this.streamToken) return
        const img = this.$refs.streamImg
        if (img) img.src = url
      })
    },

    onStreamLoad() {
      const token = this.streamToken
      if (token !== this.streamToken) return
      this._lastFrameTime = Date.now()
      this._reconnectAttempt = 0
      this.reconnecting = false
      this.errorType = ''
      this.hasFrame = true
      this.$emit('ready')
      if (!this._renderTimer) {
        this._renderTimer = setInterval(() => {
          if (token !== this.streamToken) return
          this._drawToCanvas()
        }, 100)
      }
    },

    _drawToCanvas() {
      const img = this.$refs.streamImg
      const canvas = this.$refs.previewCanvas
      if (!img || !canvas) return
      if (img.naturalWidth <= 0 || img.naturalHeight <= 0) return
      if (canvas.width !== img.naturalWidth) {
        canvas.width = img.naturalWidth
        canvas.height = img.naturalHeight
      }
      try {
        canvas.getContext('2d').drawImage(img, 0, 0)
        this._lastFrameTime = Date.now()
      } catch (e) {
        // 静默忽略,下一帧继续尝试
      }
    },

    onStreamError() {
      const token = this.streamToken
      if (token !== this.streamToken) return
      if (this._renderTimer) { clearInterval(this._renderTimer); this._renderTimer = null }
      this._scheduleReconnect()
    },

    _scheduleReconnect() {
      if (this.streamToken === 0) return
      this.reconnecting = true
      this._reconnectAttempt++
      if (this._reconnectAttempt > 10) {
        this.errorType = 'connect_fail'
        this.hasFrame = false
        this.reconnecting = false
        this.$emit('stream-broken')
        return
      }
      const img = this.$refs.streamImg
      if (img) img.src = ''
      const delay = Math.min(this._reconnectAttempt * 500, 3000)
      if (this._retryTimer) clearTimeout(this._retryTimer)
      this._retryTimer = setTimeout(() => {
        if (this.streamToken === 0) return
        this._attachStream()
      }, delay)
    },

    _heartbeatCheck() {
      if (this.streamToken === 0) return
      if (!this.hasFrame) return
      if (this.reconnecting) return
      if (Date.now() - this._lastFrameTime > 5000) {
        this._scheduleReconnect()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.camera-window {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 300px;
  background: #1a1a1a;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.5);
}
.live-stream {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  z-index: 1;
}
.hidden-stream-img {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
  top: 0;
  left: 0;
  z-index: 0;
}
.camera-placeholder {
  position: absolute;
  inset: 0;
  z-index: 2;
  color: #909399;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #1a1a1a;
}
.camera-placeholder i {
  font-size: 40px;
  margin-bottom: 10px;
}
.camera-placeholder p { margin: 0; }
.reconnect-tip {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  pointer-events: none;
  backdrop-filter: blur(4px);
  z-index: 3;
}
.occupied-banner {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(230, 162, 60, 0.9);
  color: #fff;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  pointer-events: none;
  z-index: 3;
}
</style>
