<template>
  <view class="archive-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center"><text class="nav-title">Archive</text></view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="data-engine-card glass-panel">
        <view class="engine-top">
          <view class="text-data">
            <text class="label">累计培训学时</text>
            <view class="value-box">
              <text class="number">{{ totalHours }}</text>
              <text class="unit">h</text>
            </view>
          </view>
          
          <view class="circle-chart">
            <view class="circle-bg"></view>
            <view class="circle-progress" :style="circleStyle"></view>
            <view class="circle-inner">
              <text class="percent">100%</text>
              <text class="p-label">合规率</text>
            </view>
          </view>
        </view>

        <view class="engine-bottom">
          <view class="sub-metric">
            <text class="s-val">{{ totalCount }}</text>
            <text class="s-lab">总参训次数</text>
          </view>
          <view class="sub-metric">
            <text class="s-val highlight">{{ averageScore }}</text>
            <text class="s-lab">平均考核得分</text>
          </view>
        </view>
      </view>

      <view class="action-section">
        <button class="dark-btn" :loading="isDownloading" @click="handleDownloadArchive">
          <uni-icons type="download" color="#fff" size="20" style="margin-right: 12rpx;"></uni-icons>
          <text v-if="!isDownloading">导出个人培训卷宗 (Excel)</text>
          <text v-else>卷宗生成中...</text>
        </button>
      </view>

      <view class="log-header">
        <text class="log-title">履历明细</text>
        <view class="filter-btn">
          <text>全部记录</text>
          <uni-icons type="bottom" color="#1a1a1a" size="14"></uni-icons>
        </view>
      </view>

      <view class="timeline-container">
        <view v-if="archiveList.length === 0 && !loading" class="empty-dossier">
          <uni-icons type="info" color="#cccccc" size="40"></uni-icons>
          <text>暂无相关培训档案</text>
        </view>
        
        <view v-else class="timeline-list">
          <view class="timeline-item" v-for="(item, index) in archiveList" :key="index">
            <view class="time-col">
              <text class="year">{{ formatYear(item.trainDate) }}</text>
              <text class="date">{{ formatDate(item.trainDate) }}</text>
              <view class="axis-line" v-if="index !== archiveList.length - 1"></view>
            </view>
            
            <view class="content-col">
              <view class="record-card glass-panel" @click="showDetail(item)">
                <view class="r-head">
                  <view class="r-score" :class="getScoreClass(item.score)">
                    {{ formatScore(item.score) }}
                  </view>
                  <text class="r-dept">{{ item.trainDept || item.deptName || '业务部门' }}</text>
                </view>
                
                <text class="r-subject">{{ item.subject || item.trainContent }}</text>
                
                <view class="r-meta">
                  <view class="meta-tag">
                    <uni-icons type="person" color="#606266" size="14"></uni-icons>
                    <text>{{ item.speaker || '内部讲师' }}</text>
                  </view>
                  <view class="meta-tag">
                    <uni-icons type="clock" color="#606266" size="14"></uni-icons>
                    <text>{{ item.trainHours || '0' }} 学时</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view class="load-status" v-if="archiveList.length > 0">
          <text v-if="loading">正在加载档案库...</text>
          <text v-else-if="!hasMore">—— 档案已全部加载 ——</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listMyArchive } from '@/api/biz'
import { getUserProfile } from "@/api/system/user"
import { getToken } from '@/utils/auth'
import config from '@/config'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,

      userId: null,
      userName: '我',
      
      queryParams: { pageNum: 1, pageSize: 10 },
      archiveList: [],
      total: 0,
      loading: false,
      hasMore: true,
      
      totalCount: 0,
      totalHours: '0.0',
      averageScore: '0',
      isDownloading: false
    }
  },
  computed: {
    circleStyle() {
      let percent = 100 
      let degree = (percent / 100) * 360
      return `background: conic-gradient(#1a1a1a ${degree}deg, transparent 0);`
    }
  },
  onLoad() {
    this.initSystemInfo()
    this.initUser()
    this.fetchArchives()
  },
  onPullDownRefresh() {
    this.queryParams.pageNum = 1
    this.hasMore = true
    this.fetchArchives().then(() => { uni.stopPullDownRefresh() })
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.queryParams.pageNum++
      this.fetchArchives(true)
    }
  },
  methods: {
    initSystemInfo() {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = sys.statusBarHeight || 20
      // #ifdef MP-WEIXIN
      let custom = uni.getMenuButtonBoundingClientRect()
      this.navBarHeight = custom.height + (custom.top - this.statusBarHeight) * 2
      // #endif
    },

    goBack() { uni.navigateBack({ delta: 1 }) },

    // 获取当前登录人ID，用于传给后端的导出接口
    initUser() {
      getUserProfile().then(res => {
        this.userId = res.data.userId
        this.userName = res.data.nickName || res.data.userName
      })
    },

    async fetchArchives(isAppend = false) {
      this.loading = true
      try {
        const res = await listMyArchive(this.queryParams)
        if (isAppend) {
          this.archiveList = this.archiveList.concat(res.rows)
        } else {
          this.archiveList = res.rows
          this.total = res.total
          this.calculateStats()
        }
        this.hasMore = this.archiveList.length < this.total
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
      }
    },

    async calculateStats() {
      try {
        const res = await listMyArchive({ pageNum: 1, pageSize: 1000 })
        this.totalCount = res.total
        let hours = 0, totalScore = 0, scoredCount = 0
        res.rows.forEach(item => {
          let h = parseFloat(item.trainHours)
          if (!isNaN(h)) hours += h
          let s = parseFloat(item.score)
          if (!isNaN(s)) { totalScore += s; scoredCount++ }
        })
        this.totalHours = hours.toFixed(1)
        this.averageScore = scoredCount > 0 ? Math.round(totalScore / scoredCount) : '-'
      } catch (error) {}
    },

    // 核心流文件下载逻辑（避开 request.js 的 JSON 拦截）
    async handleDownloadArchive() {
      if (!this.userId) {
        return this.$modal.msgError('未获取到身份标识，请稍后再试')
      }
      if (this.archiveList.length === 0) {
        return this.$modal.msgError('暂无数据可供导出')
      }
      
      this.isDownloading = true
      const baseURL = config.baseUrl || process.env.VUE_APP_BASE_API
      const url = baseURL + '/biz/archive/exportTemplate'

      // #ifdef H5
      try {
        // H5环境：使用 Fetch 接收 Blob
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Authorization': 'Bearer ' + getToken(),
            'Content-Type': 'application/x-www-form-urlencoded' // 必须，匹配 @RequestParam
          },
          body: 'userId=' + this.userId
        })
        const blob = await response.blob()
        const downloadUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = this.userName + '-个人培训档案.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(downloadUrl)
        this.isDownloading = false
        this.$modal.msgSuccess('导出成功')
      } catch (e) {
        this.isDownloading = false
        this.$modal.msgError('导出失败')
      }
      // #endif

      // #ifndef H5
      // 微信小程序/App 环境：使用原生 uni.request 接收 arraybuffer，并写入文件系统打开
      uni.request({
        url: url,
        method: 'POST',
        data: { userId: this.userId },
        header: {
          'Authorization': 'Bearer ' + getToken(),
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        responseType: 'arraybuffer',
        success: (res) => {
          if (res.statusCode === 200) {
            const fs = uni.getFileSystemManager()
            const filePath = `${wx.env.USER_DATA_PATH}/${this.userName}个人档案.xlsx`
            
            fs.writeFile({
              filePath: filePath,
              data: res.data,
              encoding: 'binary',
              success: () => {
                this.isDownloading = false
                // 直接唤起手机内置文档查看器打开 Excel
                uni.openDocument({
                  filePath: filePath,
                  showMenu: true,
                  success: () => { this.$modal.msgSuccess('档案已打开') }
                })
              }
            })
          } else {
            this.isDownloading = false; this.$modal.msgError('导出请求失败')
          }
        },
        fail: () => {
          this.isDownloading = false; this.$modal.msgError('网络异常')
        }
      })
      // #endif
    },

    showDetail(item) {
      if (!item.archiveId) return this.$modal.msgError('无效的档案ID')
      this.$tab.navigateTo(`/pages/biz/archive/detail?id=${item.archiveId}`)
    },

    formatYear(dateStr) { return dateStr ? dateStr.substring(0, 4) : '-' },
    formatDate(dateStr) { return dateStr ? dateStr.substring(5, 10).replace('-', '.') : '-' },
    formatScore(score) {
      if (!score || score === 'null') return '已入档'
      const num = parseFloat(score)
      if (!isNaN(num)) return `${num} 分`
      return score
    },
    getScoreClass(score) {
      const num = parseFloat(score)
      if (!isNaN(num)) {
        if (num >= 90) return 'score-excellent'
        if (num < 60) return 'score-danger'
      }
      return 'score-normal'
    }
  }
}
</script>

<style lang="scss" scoped>
.archive-page {
  width: 100vw; min-height: 100vh; background-color: #f8faff; position: relative; overflow-x: hidden; padding-bottom: 80rpx;
  .blob { position: absolute; width: 600rpx; height: 600rpx; background: linear-gradient(135deg, rgba(60, 156, 255, 0.12), rgba(122, 187, 255, 0.05)); filter: blur(90px); border-radius: 50%; z-index: 0; animation: move 18s infinite alternate; }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 300rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }
  @keyframes move { from { transform: translate(0, 0) scale(1); } to { transform: translate(40rpx, 80rpx) scale(1.05); } }

  .custom-navbar { position: fixed; top: 0; left: 0; right: 0; z-index: 999; display: flex; align-items: center; justify-content: space-between; padding: 0 30rpx; }
  .glass-nav { background: rgba(248, 250, 255, 0.85); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0, 0, 0, 0.02); }
  .nav-left, .nav-right { width: 80rpx; display: flex; align-items: center; }
  .nav-center { flex: 1; display: flex; justify-content: center; .nav-title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 0.5px; } }

  .main-container { position: relative; z-index: 1; width: 88%; max-width: 680rpx; margin: 0 auto; }
  .glass-panel { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(25px); border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03); }

  .data-engine-card { border-radius: 40rpx; padding: 50rpx 40rpx; margin-bottom: 50rpx;
    .engine-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40rpx;
      .text-data { display: flex; flex-direction: column;
        .label { font-size: 24rpx; color: #909399; font-weight: 700; margin-bottom: 8rpx; letter-spacing: 1px; }
        .value-box { display: flex; align-items: flex-end;
          .number { font-size: 64rpx; font-weight: 900; color: #1a1a1a; line-height: 1; font-family: monospace; letter-spacing: -2px; }
          .unit { font-size: 28rpx; color: #1a1a1a; font-weight: 700; margin-left: 10rpx; padding-bottom: 8rpx; }
        }
      }
      .circle-chart { position: relative; width: 140rpx; height: 140rpx; border-radius: 50%;
        .circle-bg { position: absolute; top:0; left:0; width: 100%; height: 100%; border-radius: 50%; background: rgba(0,0,0,0.04); }
        .circle-progress { position: absolute; top:0; left:0; width: 100%; height: 100%; border-radius: 50%; mask: radial-gradient(transparent 56rpx, #000 58rpx); -webkit-mask: radial-gradient(transparent 56rpx, #000 58rpx); }
        .circle-inner { position: absolute; top: 10rpx; left: 10rpx; right: 10rpx; bottom: 10rpx; background: #fff; border-radius: 50%; display: flex; flex-direction: column; justify-content: center; align-items: center; box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05);
          .percent { font-size: 28rpx; font-weight: 900; color: #1a1a1a; }
          .p-label { font-size: 18rpx; color: #909399; font-weight: 600; margin-top: 2rpx; }
        }
      }
    }
    .engine-bottom { display: flex; border-top: 1px dashed rgba(0,0,0,0.08); padding-top: 30rpx;
      .sub-metric { flex: 1; display: flex; flex-direction: column;
        .s-val { font-size: 36rpx; font-weight: 800; color: #1a1a1a; font-family: monospace; margin-bottom: 4rpx; }
        .s-lab { font-size: 22rpx; color: #909399; font-weight: 600; }
        .highlight { color: #3c9cff; }
      }
    }
  }

  .action-section { margin-bottom: 60rpx;
    .dark-btn { width: 100%; height: 100rpx; background: #1a1a1a; border-radius: 28rpx; display: flex; justify-content: center; align-items: center; color: #fff; font-size: 28rpx; font-weight: 700; border: none; transition: all 0.2s; box-shadow: 0 20rpx 40rpx rgba(26, 26, 26, 0.15);
      &::after { border: none; } &:active { transform: scale(0.97); box-shadow: 0 10rpx 20rpx rgba(26, 26, 26, 0.1); }
    }
  }

  .log-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40rpx;
    .log-title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; }
    .filter-btn { display: flex; align-items: center; background: rgba(0,0,0,0.04); padding: 10rpx 20rpx; border-radius: 20rpx; text { font-size: 22rpx; font-weight: 600; color: #1a1a1a; margin-right: 8rpx; } }
  }

  .timeline-container {
    .empty-dossier { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; text { font-size: 26rpx; color: #909399; margin-top: 24rpx; } }
    .timeline-list { display: flex; flex-direction: column; }
    .timeline-item { display: flex; margin-bottom: 40rpx;
      .time-col { width: 100rpx; display: flex; flex-direction: column; align-items: center; margin-right: 20rpx;
        .year { font-size: 22rpx; color: #909399; font-weight: 700; font-family: monospace; margin-bottom: 4rpx; }
        .date { font-size: 32rpx; color: #1a1a1a; font-weight: 900; font-family: monospace; letter-spacing: -1px; }
        .axis-line { width: 2rpx; flex: 1; background: rgba(0,0,0,0.05); margin-top: 16rpx; min-height: 60rpx; }
      }
      .content-col { flex: 1;
        .record-card { border-radius: 32rpx; padding: 36rpx; transition: transform 0.2s; &:active { transform: scale(0.98); }
          .r-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx;
            .r-score { padding: 6rpx 16rpx; border-radius: 12rpx; font-size: 24rpx; font-weight: 800; font-family: monospace; }
            .score-excellent { background: rgba(26,26,26,0.06); color: #1a1a1a; }
            .score-normal { background: rgba(144,147,153,0.1); color: #606266; }
            .score-danger { background: rgba(250,53,52,0.1); color: #fa3534; }
            .r-dept { font-size: 24rpx; color: #909399; font-weight: 600; }
          }
          .r-subject { display: block; font-size: 30rpx; font-weight: 800; color: #1a1a1a; line-height: 1.4; margin-bottom: 24rpx; }
          .r-meta { display: flex; flex-wrap: wrap; gap: 16rpx;
            .meta-tag { display: flex; align-items: center; background: #f4f4f5; padding: 8rpx 16rpx; border-radius: 12rpx; text { font-size: 22rpx; color: #606266; font-weight: 600; margin-left: 6rpx; } }
          }
        }
      }
    }
    .load-status { text-align: center; padding: 20rpx 0; text { font-size: 24rpx; color: #ccc; font-weight: 600; letter-spacing: 2px; } }
  }
}
</style>