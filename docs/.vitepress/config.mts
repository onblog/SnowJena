import { defineConfig } from 'vitepress'

const github = 'https://github.com/onblog/java-limiter'

export default defineConfig({
  title: 'java-limiter',
  description:
    'Lock-free rate limiting for Java 8+: token bucket, leaky bucket, sliding window and fixed window. One jar, zero dependencies.',
  lang: 'en',
  cleanUrls: true,
  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/logo.svg' }],
    ['meta', { name: 'theme-color', content: '#0f172a' }]
  ],
  themeConfig: {
    logo: '/logo.svg',
    socialLinks: [{ icon: 'github', link: github }],
    search: { provider: 'local' }
  },
  locales: {
    '/en/': {
      label: 'English',
      lang: 'en',
      link: '/en/',
      themeConfig: {
        nav: [
          { text: 'Guide', link: '/en/guide' },
          { text: 'Algorithms', link: '/en/algorithms' },
          { text: 'API', link: '/en/api' },
          { text: 'FAQ', link: '/en/faq' }
        ],
        sidebar: [
          {
            text: 'Documentation',
            items: [
              { text: 'Introduction', link: '/en/' },
              { text: 'Guide', link: '/en/guide' },
              { text: 'Algorithms', link: '/en/algorithms' },
              { text: 'API reference', link: '/en/api' },
              { text: 'FAQ', link: '/en/faq' }
            ]
          }
        ],
        outline: { label: 'On this page' },
        docFooter: { prev: 'Previous', next: 'Next' },
        returnToTopLabel: 'Back to top',
        darkModeSwitchLabel: 'Theme',
        sidebarMenuLabel: 'Menu'
      }
    },
    '/zh-CN/': {
      label: '简体中文',
      lang: 'zh-CN',
      link: '/zh-CN/',
      themeConfig: {
        nav: [
          { text: '指南', link: '/zh-CN/guide' },
          { text: '算法', link: '/zh-CN/algorithms' },
          { text: 'API', link: '/zh-CN/api' },
          { text: '常见问题', link: '/zh-CN/faq' }
        ],
        sidebar: [
          {
            text: '文档',
            items: [
              { text: '简介', link: '/zh-CN/' },
              { text: '使用指南', link: '/zh-CN/guide' },
              { text: '算法原理', link: '/zh-CN/algorithms' },
              { text: 'API 参考', link: '/zh-CN/api' },
              { text: '常见问题', link: '/zh-CN/faq' }
            ]
          }
        ],
        outline: { label: '本页目录' },
        docFooter: { prev: '上一篇', next: '下一篇' },
        returnToTopLabel: '回到顶部',
        darkModeSwitchLabel: '主题',
        sidebarMenuLabel: '菜单'
      }
    }
  }
})
