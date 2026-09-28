package com.h5.controller.user;

import com.h5.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 帮助中心 FAQ Controller
 * FAQ数据暂为静态配置，后续可迁移到数据库faq表
 */
@RestController
@RequestMapping("/wap/help")
public class HelpController {

    /**
     * FAQ分类列表
     * GET /api/wap/help/categories
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildCategory("account", "账号问题", "👤"));
        list.add(buildCategory("recharge", "充值问题", "💰"));
        list.add(buildCategory("withdraw", "提现问题", "🏦"));
        list.add(buildCategory("game", "游戏玩法", "🎮"));
        list.add(buildCategory("agent", "代理推广", "🤝"));
        list.add(buildCategory("other", "其他问题", "📋"));
        return Result.success(list);
    }

    /**
     * FAQ列表（按分类筛选）
     * GET /api/wap/help/list?category=account
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) String category) {
        List<Map<String, Object>> all = getFaqList();
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> faq : all) {
            if (category == null || category.isEmpty() || category.equals(faq.get("category"))) {
                filtered.add(faq);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", filtered);
        data.put("total", filtered.size());
        return Result.success(data);
    }

    /**
     * FAQ详情
     * GET /api/wap/help/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        for (Map<String, Object> faq : getFaqList()) {
            if (id.equals(faq.get("id"))) {
                return Result.success(faq);
            }
        }
        return Result.error(404, "FAQ不存在");
    }

    // ==================== 静态FAQ数据 ====================

    private Map<String, Object> buildCategory(String code, String name, String icon) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("name", name);
        m.put("icon", icon);
        return m;
    }

    private List<Map<String, Object>> getFaqList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildFaq(1L, "account", "如何注册账号？", "点击首页注册按钮，输入用户名和密码即可完成注册。推荐使用邀请码注册，可获得新人奖励。"));
        list.add(buildFaq(2L, "account", "忘记密码怎么办？", "点击登录页忘记密码，通过绑定的手机号或邮箱验证后重置密码。如未绑定，请联系在线客服。"));
        list.add(buildFaq(3L, "account", "如何修改个人资料？", "进入个人中心→设置→个人资料，可修改昵称、头像、绑定手机号等信息。用户名不可修改。"));
        list.add(buildFaq(4L, "recharge", "支持哪些充值方式？", "支持USDT-TRC20、USDT-ERC20、支付宝、微信、银行卡等多种充值方式。USDT充值自动到账，其他方式人工审核。"));
        list.add(buildFaq(5L, "recharge", "充值多久到账？", "USDT充值确认后自动到账（通常1-3分钟）。其他方式充值提交后，客服会在5-30分钟内审核到账。"));
        list.add(buildFaq(6L, "recharge", "最低充值金额是多少？", "不同充值方式最低金额不同。USDT最低10 USDT，支付宝/微信最低100元，银行卡最低500元。"));
        list.add(buildFaq(7L, "withdraw", "如何申请提现？", "进入个人中心→钱包→提现，选择提现方式，输入金额和收款账户，提交后等待审核。首次提现需完成实名认证。"));
        list.add(buildFaq(8L, "withdraw", "提现多久到账？", "提现申请提交后，客服会在1-2小时内审核。USDT提现审核后10分钟内到账，银行卡提现1-24小时到账。"));
        list.add(buildFaq(9L, "withdraw", "提现手续费是多少？", "USDT提现免手续费。银行卡提现收取1%手续费，最低5元。具体以提现页面显示为准。"));
        list.add(buildFaq(10L, "game", "如何参与投注？", "进入游戏大厅选择彩种，选择玩法和号码，输入投注金额，确认提交即可。投注后余额即时扣减。"));
        list.add(buildFaq(11L, "game", "开奖时间是什么时候？", "不同彩种开奖时间不同。六合彩每周二、四、六21:30开奖；其他彩种详见游戏页面开奖倒计时。"));
        list.add(buildFaq(12L, "game", "中奖后奖金如何发放？", "开奖后系统自动结算，中奖金额即时发放到账户余额，可用于继续投注或提现。"));
        list.add(buildFaq(13L, "agent", "如何成为代理？", "注册账号后自动获得代理资格。进入代理中心可查看邀请码和推广链接，邀请好友注册即可获得返佣。"));
        list.add(buildFaq(14L, "agent", "代理返佣比例是多少？", "代理返佣比例根据团队充值额阶梯计算，最低1%，最高3%。团队充值越多，返佣比例越高。"));
        list.add(buildFaq(15L, "agent", "佣金如何提现？", "进入代理中心→佣金提现，输入金额提交即可。佣金可直接提现到USDT地址或银行卡。"));
        list.add(buildFaq(16L, "other", "如何联系客服？", "平台提供7×24小时在线客服。点击页面右下角客服图标，或通过Telegram联系官方客服。"));
        list.add(buildFaq(17L, "other", "平台是否安全可靠？", "平台采用银行级SSL加密，资金冷存储，定期安全审计。用户数据严格保密，不会泄露给第三方。"));
        list.add(buildFaq(18L, "other", "什么是试玩账号？", "试玩账号可体验平台所有功能，但余额为虚拟资金，不可充值提现。注册正式账号后可正常使用资金功能。"));
        return list;
    }

    private Map<String, Object> buildFaq(Long id, String category, String question, String answer) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("category", category);
        m.put("question", question);
        m.put("answer", answer);
        m.put("views", 0);
        return m;
    }
}
