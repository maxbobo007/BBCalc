App({
  globalData: { cloudReady: false },

  onLaunch() {
    if (wx.cloud) {
      try {
        wx.cloud.init({ env: wx.cloud.DYNAMIC_CURRENT_ENV, traceUser: false });
        this.globalData.cloudReady = true;
      } catch (e) {
        this.globalData.cloudReady = false;
      }
    }
  },
});
