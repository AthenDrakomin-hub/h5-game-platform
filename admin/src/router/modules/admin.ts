import { $t } from "@/plugins/i18n";
const Layout = () => import("@/layout/index.vue");

export default {
  path: "/admin",
  name: "Admin",
  component: Layout,
  redirect: "/admin/dashboard",
  meta: {
    icon: "ep/setting",
    title: "管理后台",
    rank: 10
  },
  children: [
    {
      path: "/admin/dashboard",
      name: "AdminDashboard",
      component: () => import("@/views/admin/Dashboard.vue"),
      meta: {
        icon: "ep/data-analysis",
        title: "仪表盘",
        keepAlive: true
      }
    },
    {
      path: "/admin/users",
      name: "AdminUsers",
      component: () => import("@/views/admin/UserManagement.vue"),
      meta: {
        icon: "ep/user",
        title: "用户管理",
        keepAlive: true
      }
    },
    {
      path: "/admin/orders",
      name: "AdminOrders",
      component: () => import("@/views/admin/OrderManagement.vue"),
      meta: {
        icon: "ep/document",
        title: "订单管理",
        keepAlive: true
      }
    },
    {
      path: "/admin/games",
      name: "AdminGames",
      component: () => import("@/views/admin/GameManagement.vue"),
      meta: {
        icon: "ep/game",
        title: "游戏管理",
        keepAlive: true
      }
    },
    {
      path: "/admin/promos",
      name: "AdminPromos",
      component: () => import("@/views/admin/PromoManagement.vue"),
      meta: {
        icon: "ep/present",
        title: "活动管理",
        keepAlive: true
      }
    },
    {
      path: "/admin/messages",
      name: "AdminMessages",
      component: () => import("@/views/admin/MessageManagement.vue"),
      meta: {
        icon: "ep/message",
        title: "消息管理",
        keepAlive: true
      }
    }
  ]
} satisfies RouteConfigsTable;
