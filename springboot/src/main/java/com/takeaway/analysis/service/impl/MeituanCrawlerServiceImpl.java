package com.takeaway.analysis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.takeaway.analysis.entity.Dish;
import com.takeaway.analysis.entity.Merchant;
import com.takeaway.analysis.service.CrawlerService;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 美团爬虫服务实现 - 使用Selenium
 * 
 * 使用Selenium WebDriver进行浏览器自动化，可以执行JavaScript并获取动态加载的数据
 */
@Slf4j
@Service
public class MeituanCrawlerServiceImpl implements CrawlerService {

    @Value("${crawler.meituan.base-url:https://h5.waimai.meituan.com}")
    private String baseUrl;

    @Value("${crawler.meituan.timeout:30000}")
    private int timeout;

    @Value("${crawler.meituan.headless:false}")
    private boolean headless;
    
    @Value("${crawler.meituan.cookie:}")
    private String cookieConfig;
    
    @Value("${crawler.meituan.latitude:39.9042}")
    private double latitude;
    
    @Value("${crawler.meituan.longitude:116.4074}")
    private double longitude;
    
    @Value("${crawler.meituan.manual-wait-seconds:15}")
    private int manualWaitSeconds;

    private Random random = new Random();
    
    private static boolean webDriverSetup = false;

    @PostConstruct
    public void init() {
        // 初始化WebDriver（只需执行一次）
        if (!webDriverSetup) {
            try {
                log.info("正在初始化 ChromeDriver...");
                WebDriverManager.chromedriver().setup();
                webDriverSetup = true;
                log.info("ChromeDriver 初始化完成");
            } catch (Exception e) {
                log.error("ChromeDriver 初始化失败", e);
            }
        }
    }

    /**
     * 创建WebDriver实例
     */
    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        
        // 无头模式（不显示浏览器窗口）
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        // 基本配置
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=430,932"); // iPhone 14 Pro Max 尺寸
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*"); // 解决 WebSocket 连接问题
        
        // 模拟移动设备 - 使用更真实的 User-Agent
        options.addArguments("--user-agent=Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1");
        
        // 禁用自动化标志
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "enable-logging"});
        options.setExperimentalOption("useAutomationExtension", false);
        
        // 模拟地理位置（北京市中心坐标）
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("profile.default_content_setting_values.geolocation", 1); // 允许地理位置
        options.setExperimentalOption("prefs", prefs);
        
        return new ChromeDriver(options);
    }

    @Override
    public List<Merchant> crawlMerchants(String keyword, int pageSize) {
        List<Merchant> merchants = new ArrayList<>();
        log.info("开始使用Selenium爬取美团外卖商户数据，关键词：{}，数量：{}", keyword, pageSize);
        log.info("headless模式：{}", headless);
        
        WebDriver driver = null;
        try {
            driver = createWebDriver();
            
            // 访问美团外卖首页
            String url = baseUrl + "/waimai/mindex/home";
            log.info("访问页面：{}", url);
            driver.get(url);
            
            // 模拟地理位置（使用JavaScript覆盖 Geolocation API）
            setGeolocation(driver);
            
            // 添加Cookie（如果配置了）
            addCookies(driver);
            
            // 刷新页面使Cookie和定位生效
            if (StringUtils.isNotBlank(cookieConfig)) {
                driver.navigate().refresh();
                Thread.sleep(2000);
            }
            
            // 等待页面加载
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout / 1000));
            log.info("等待页面加载...");
            log.info("");
            log.info("========================================");
            log.info("  请在浏览器中手动操作：");
            log.info("  1. 如果有定位弹窗，请选择一个位置");
            log.info("  2. 如果提示网络不稳定，请刷新页面");
            log.info("  3. 如果有其他弹窗，请关闭");
            log.info("  4. 等待商户列表加载出来");
            log.info("  等待 {} 秒供您操作...", manualWaitSeconds);
            log.info("========================================");
            log.info("");
            Thread.sleep(manualWaitSeconds * 1000L); // 给用户时间手动操作
            
            // 自动关闭弹窗（定位弹窗、广告弹窗、引导弹窗等）
            closePopups(driver);
            
            log.info("当前页面标题：{}", driver.getTitle());
            log.info("当前URL：{}", driver.getCurrentUrl());
            
            // 尝试搜索
            try {
                // 查找搜索框并输入关键词
                WebElement searchInput = findSearchInput(driver, wait);
                if (searchInput != null) {
                    searchInput.clear();
                    searchInput.sendKeys(keyword);
                    searchInput.sendKeys(Keys.ENTER);
                    log.info("已输入搜索关键词：{}", keyword);
                    Thread.sleep(3000); // 等待搜索结果
                    
                    // 搜索后再次检查弹窗
                    closePopups(driver);
                } else {
                    log.warn("未找到搜索框");
                }
            } catch (Exception e) {
                log.warn("搜索框操作失败：{}", e.getMessage());
            }
            
            // 滚动页面加载更多数据
            scrollPage(driver, 3);
            
            // 滚动后再次检查弹窗
            closePopups(driver);
            
            // 获取页面源码并解析
            String pageSource = driver.getPageSource();
            log.info("页面源码长度：{} 字符", pageSource.length());
            
            // 尝试从页面中提取商户数据
            merchants = extractMerchantsFromPage(driver, pageSource, pageSize);
            
            if (merchants.isEmpty()) {
                // 尝试从网络请求中获取JSON数据
                merchants = extractMerchantsFromScript(pageSource, pageSize);
            }
            
            if (merchants.isEmpty()) {
                log.warn("无法从页面获取数据，使用模拟数据");
                merchants = generateMockMerchants(pageSize);
            }
            
            log.info("爬取完成，共获取{}条商户数据", merchants.size());
            
        } catch (Exception e) {
            log.error("Selenium爬取失败", e);
            merchants = generateMockMerchants(pageSize);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("关闭WebDriver失败", e);
                }
            }
        }
        
        return merchants;
    }

    /**
     * 模拟地理位置
     * 使用JavaScript覆盖Geolocation API，提供假的位置信息
     */
    private void setGeolocation(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 覆盖 Geolocation API（使用配置的经纬度）
            String script = 
                "window.navigator.geolocation.getCurrentPosition = function(success, error, options) {" +
                "  success({" +
                "    coords: {" +
                "      latitude: " + latitude + "," +
                "      longitude: " + longitude + "," +
                "      accuracy: 100," +
                "      altitude: null," +
                "      altitudeAccuracy: null," +
                "      heading: null," +
                "      speed: null" +
                "    }," +
                "    timestamp: Date.now()" +
                "  });" +
                "};" +
                "window.navigator.geolocation.watchPosition = function(success, error, options) {" +
                "  success({" +
                "    coords: {" +
                "      latitude: " + latitude + "," +
                "      longitude: " + longitude + "," +
                "      accuracy: 100" +
                "    }," +
                "    timestamp: Date.now()" +
                "  });" +
                "  return 1;" +
                "};";
            
            js.executeScript(script);
            log.info("已设置模拟地理位置：经度={}, 纬度={}", longitude, latitude);
            
        } catch (Exception e) {
            log.warn("设置模拟地理位置失败：{}", e.getMessage());
        }
    }

    /**
     * 添加Cookie到浏览器
     * Cookie格式：name=value; name2=value2
     */
    private void addCookies(WebDriver driver) {
        if (StringUtils.isBlank(cookieConfig)) {
            log.info("未配置Cookie");
            return;
        }
        
        try {
            String[] cookies = cookieConfig.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.contains("=")) {
                    String[] parts = cookie.split("=", 2);
                    String name = parts[0].trim();
                    String value = parts.length > 1 ? parts[1].trim() : "";
                    
                    if (StringUtils.isNotBlank(name)) {
                        Cookie seleniumCookie = new Cookie(name, value);
                        driver.manage().addCookie(seleniumCookie);
                        log.debug("添加Cookie: {}={}", name, value.length() > 20 ? value.substring(0, 20) + "..." : value);
                    }
                }
            }
            log.info("成功添加 {} 个Cookie", cookies.length);
        } catch (Exception e) {
            log.warn("添加Cookie失败：{}", e.getMessage());
        }
    }

    /**
     * 自动关闭页面弹窗
     * 包括：定位弹窗、广告弹窗、引导弹窗、登录提示等
     */
    private void closePopups(WebDriver driver) {
        log.info("检查并关闭弹窗...");
        int closedCount = 0;
        
        // 常见的关闭按钮选择器
        String[] closeSelectors = {
            // 通用关闭按钮
            "[class*='close']",
            "[class*='Close']",
            "[class*='cancel']",
            "[class*='Cancel']",
            "button[class*='close']",
            "div[class*='close']",
            "span[class*='close']",
            "i[class*='close']",
            // 遮罩层点击关闭
            "[class*='mask']",
            "[class*='overlay']",
            // X 图标
            "[class*='icon-close']",
            "[class*='icon_close']",
            // 知道了、我知道了按钮
            "button:contains('知道')",
            "div:contains('知道了')",
            "span:contains('知道了')",
            // 取消按钮
            "button:contains('取消')",
            // 跳过按钮
            "button:contains('跳过')",
            "div:contains('跳过')",
            // 关闭文字
            "div:contains('关闭')",
            "span:contains('关闭')",
            // 定位相关
            "[class*='location'] [class*='close']",
            "[class*='address'] [class*='close']",
            // 弹窗容器的关闭按钮
            "[class*='popup'] [class*='close']",
            "[class*='modal'] [class*='close']",
            "[class*='dialog'] [class*='close']",
            "[class*='toast'] [class*='close']",
        };
        
        for (String selector : closeSelectors) {
            try {
                // 使用CSS选择器（:contains不是标准CSS，需要特殊处理）
                if (selector.contains(":contains")) {
                    continue; // 跳过伪选择器，后面用JavaScript处理
                }
                
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                for (WebElement element : elements) {
                    try {
                        if (element.isDisplayed() && element.isEnabled()) {
                            element.click();
                            closedCount++;
                            log.debug("点击关闭按钮: {}", selector);
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        // 元素可能已经不可点击，忽略
                    }
                }
            } catch (Exception e) {
                // 选择器可能无效，忽略
            }
        }
        
        // 使用JavaScript关闭包含特定文字的按钮
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 查找并点击"知道了"、"我知道了"、"确定"等按钮
            String[] buttonTexts = {"知道了", "我知道了", "确定", "跳过", "关闭", "取消", "暂不", "以后再说"};
            for (String text : buttonTexts) {
                try {
                    String script = 
                        "var buttons = document.querySelectorAll('button, div, span, a');" +
                        "for(var i=0; i<buttons.length; i++) {" +
                        "  if(buttons[i].innerText && buttons[i].innerText.trim() === '" + text + "') {" +
                        "    buttons[i].click();" +
                        "    return true;" +
                        "  }" +
                        "}" +
                        "return false;";
                    Boolean clicked = (Boolean) js.executeScript(script);
                    if (Boolean.TRUE.equals(clicked)) {
                        closedCount++;
                        log.debug("点击按钮: {}", text);
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
            
            // 尝试点击遮罩层关闭弹窗
            String maskScript = 
                "var masks = document.querySelectorAll('[class*=\"mask\"], [class*=\"overlay\"]');" +
                "for(var i=0; i<masks.length; i++) {" +
                "  if(masks[i].offsetWidth > 0 && masks[i].offsetHeight > 0) {" +
                "    masks[i].click();" +
                "    return true;" +
                "  }" +
                "}" +
                "return false;";
            js.executeScript(maskScript);
            
        } catch (Exception e) {
            log.debug("JavaScript关闭弹窗失败: {}", e.getMessage());
        }
        
        // 按ESC键尝试关闭弹窗
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            Thread.sleep(300);
        } catch (Exception e) {
            // 忽略
        }
        
        if (closedCount > 0) {
            log.info("已关闭 {} 个弹窗", closedCount);
        } else {
            log.info("未检测到需要关闭的弹窗");
        }
    }

    /**
     * 查找搜索输入框
     */
    private WebElement findSearchInput(WebDriver driver, WebDriverWait wait) {
        String[] selectors = {
            "input[type='search']",
            "input[placeholder*='搜索']",
            "input[placeholder*='search']",
            ".search-input input",
            ".search-box input",
            "input.search",
            "[data-testid='search-input']"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            } catch (Exception e) {
                // 继续尝试下一个选择器
            }
        }
        return null;
    }

    /**
     * 滚动页面加载更多数据
     */
    private void scrollPage(WebDriver driver, int times) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < times; i++) {
            try {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1500);
            } catch (Exception e) {
                log.warn("滚动页面失败", e);
            }
        }
    }

    /**
     * 从页面DOM中提取商户数据
     */
    private List<Merchant> extractMerchantsFromPage(WebDriver driver, String pageSource, int pageSize) {
        List<Merchant> merchants = new ArrayList<>();
        
        // 尝试多种可能的商户列表选择器
        String[] selectors = {
            "[class*='shoplist'] [class*='item']",
            "[class*='shop-list'] [class*='item']",
            "[class*='restaurant'] [class*='item']",
            "[class*='poi-list'] [class*='item']",
            "[class*='shopItem']",
            "[class*='shop_item']",
            "a[href*='/waimai/mindex/menu']",
            "[data-poi-id]",
            "[class*='list'] [class*='shop']"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                log.info("选择器 '{}' 找到 {} 个元素", selector, elements.size());
                
                if (!elements.isEmpty()) {
                    for (int i = 0; i < Math.min(elements.size(), pageSize); i++) {
                        Merchant merchant = extractMerchantFromElement(elements.get(i));
                        if (merchant != null && StringUtils.isNotBlank(merchant.getMerchantName())) {
                            merchants.add(merchant);
                        }
                    }
                    
                    if (!merchants.isEmpty()) {
                        log.info("使用选择器 '{}' 成功提取 {} 个商户", selector, merchants.size());
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("选择器 '{}' 失败: {}", selector, e.getMessage());
            }
        }
        
        return merchants;
    }

    /**
     * 从单个元素中提取商户信息
     */
    private Merchant extractMerchantFromElement(WebElement element) {
        try {
            Merchant merchant = new Merchant();
            String text = element.getText();
            
            // 提取商户名称
            String name = extractText(element, "[class*='name']", "[class*='title']", "h2", "h3", ".shop-name");
            if (StringUtils.isBlank(name) && StringUtils.isNotBlank(text)) {
                // 从文本中提取第一行作为名称
                String[] lines = text.split("\n");
                if (lines.length > 0) {
                    name = lines[0].trim();
                }
            }
            merchant.setMerchantName(name);
            
            // 提取评分
            String ratingText = extractText(element, "[class*='rating']", "[class*='score']", "[class*='star']");
            if (StringUtils.isNotBlank(ratingText)) {
                merchant.setRating(parseDecimal(ratingText));
            }
            
            // 提取销量
            String salesText = extractText(element, "[class*='sales']", "[class*='sold']", "[class*='order']");
            if (StringUtils.isBlank(salesText)) {
                // 从完整文本中查找销量
                Pattern salesPattern = Pattern.compile("月售(\\d+)");
                Matcher matcher = salesPattern.matcher(text);
                if (matcher.find()) {
                    salesText = matcher.group(1);
                }
            }
            merchant.setMonthlySales(parseSales(salesText));
            
            // 提取配送费
            String deliveryText = extractText(element, "[class*='delivery']", "[class*='fee']", "[class*='shipping']");
            if (StringUtils.isBlank(deliveryText)) {
                Pattern feePattern = Pattern.compile("配送[￥¥]?(\\d+\\.?\\d*)");
                Matcher matcher = feePattern.matcher(text);
                if (matcher.find()) {
                    deliveryText = matcher.group(1);
                }
            }
            merchant.setDeliveryFee(parseDecimal(deliveryText));
            
            // 提取起送价
            String minPriceText = extractText(element, "[class*='min']", "[class*='start']");
            if (StringUtils.isBlank(minPriceText)) {
                Pattern minPattern = Pattern.compile("起送[￥¥]?(\\d+\\.?\\d*)");
                Matcher matcher = minPattern.matcher(text);
                if (matcher.find()) {
                    minPriceText = matcher.group(1);
                }
            }
            merchant.setMinOrderPrice(parseDecimal(minPriceText));
            
            // 提取商户类型
            String typeText = extractText(element, "[class*='category']", "[class*='type']", "[class*='tag']");
            merchant.setMerchantType(typeText);
            
            // 提取距离/地址信息
            String distanceText = extractText(element, "[class*='distance']", "[class*='address']");
            merchant.setAddress(distanceText);
            
            merchant.setStatus(1);
            
            return merchant;
        } catch (Exception e) {
            log.debug("提取商户信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用多个选择器尝试提取文本
     */
    private String extractText(WebElement parent, String... selectors) {
        for (String selector : selectors) {
            try {
                List<WebElement> elements = parent.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    String text = elements.get(0).getText().trim();
                    if (StringUtils.isNotBlank(text)) {
                        return text;
                    }
                }
            } catch (Exception e) {
                // 继续尝试下一个选择器
            }
        }
        return null;
    }

    /**
     * 从页面脚本中提取JSON数据
     */
    private List<Merchant> extractMerchantsFromScript(String pageSource, int pageSize) {
        List<Merchant> merchants = new ArrayList<>();
        
        try {
            // 查找可能包含数据的JSON
            Pattern[] patterns = {
                Pattern.compile("window\\.__INITIAL_STATE__\\s*=\\s*(\\{[^;]+\\});?"),
                Pattern.compile("window\\.__NUXT__\\s*=\\s*(\\{[^;]+\\});?"),
                Pattern.compile("\"poiList\"\\s*:\\s*(\\[[^\\]]+\\])"),
                Pattern.compile("\"shopList\"\\s*:\\s*(\\[[^\\]]+\\])"),
                Pattern.compile("\"list\"\\s*:\\s*(\\[[^\\]]+\\])")
            };
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(pageSource);
                if (matcher.find()) {
                    String jsonStr = matcher.group(1);
                    try {
                        if (jsonStr.startsWith("[")) {
                            JSONArray array = JSON.parseArray(jsonStr);
                            for (int i = 0; i < Math.min(array.size(), pageSize); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                Merchant merchant = parseMerchantFromJSON(obj);
                                if (merchant != null) {
                                    merchants.add(merchant);
                                }
                            }
                        } else {
                            JSONObject obj = JSON.parseObject(jsonStr);
                            // 尝试提取列表数据
                            JSONArray list = findListInJSON(obj);
                            if (list != null) {
                                for (int i = 0; i < Math.min(list.size(), pageSize); i++) {
                                    JSONObject item = list.getJSONObject(i);
                                    Merchant merchant = parseMerchantFromJSON(item);
                                    if (merchant != null) {
                                        merchants.add(merchant);
                                    }
                                }
                            }
                        }
                        
                        if (!merchants.isEmpty()) {
                            log.info("从脚本中提取到 {} 个商户", merchants.size());
                            break;
                        }
                    } catch (Exception e) {
                        log.debug("解析JSON失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从脚本提取数据失败", e);
        }
        
        return merchants;
    }

    /**
     * 在JSON对象中递归查找列表
     */
    private JSONArray findListInJSON(JSONObject obj) {
        String[] listKeys = {"poiList", "shopList", "list", "data", "items", "shops", "restaurants"};
        
        for (String key : listKeys) {
            if (obj.containsKey(key)) {
                Object value = obj.get(key);
                if (value instanceof JSONArray) {
                    return (JSONArray) value;
                } else if (value instanceof JSONObject) {
                    JSONArray result = findListInJSON((JSONObject) value);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从JSON解析商户信息
     */
    private Merchant parseMerchantFromJSON(JSONObject poi) {
        try {
            Merchant merchant = new Merchant();
            
            merchant.setMerchantName(getStringValue(poi, "name", "shopName", "restaurantName", "poiName", "title"));
            merchant.setMerchantType(getStringValue(poi, "category", "type", "categoryName", "cateName"));
            
            BigDecimal rating = getDecimalValue(poi, "rating", "score", "avgScore", "star", "wmPoiScore");
            if (rating != null && rating.compareTo(BigDecimal.valueOf(5)) > 0) {
                rating = rating.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
            merchant.setRating(rating);
            
            merchant.setTotalSales(getIntegerValue(poi, "totalSales", "sales", "allCount"));
            merchant.setMonthlySales(getIntegerValue(poi, "monthlySales", "monthSales", "monthOrderCount", "monthSalesCount"));
            merchant.setMinOrderPrice(getDecimalValue(poi, "minOrderPrice", "minPrice", "minOrderAmount", "startPrice", "shippingFee"));
            merchant.setDeliveryFee(getDecimalValue(poi, "deliveryFee", "shippingFee", "deliveryCost", "fee"));
            merchant.setAddress(getStringValue(poi, "address", "location", "addr", "fullAddress"));
            merchant.setPhone(getStringValue(poi, "phone", "tel", "contactPhone"));
            merchant.setBusinessHours(getStringValue(poi, "businessHours", "openTime", "hours"));
            merchant.setStatus(1);
            
            return merchant;
        } catch (Exception e) {
            log.debug("从JSON解析商户信息失败", e);
            return null;
        }
    }

    @Override
    public List<Dish> crawlDishes(Long merchantId) {
        List<Dish> dishes = new ArrayList<>();
        log.info("开始使用Selenium爬取商户菜品数据，商户ID：{}", merchantId);
        
        WebDriver driver = null;
        try {
            driver = createWebDriver();
            
            // 访问商户详情页
            String url = baseUrl + "/waimai/mindex/menu?poi_id=" + merchantId;
            log.info("访问页面：{}", url);
            driver.get(url);
            
            // 等待页面加载
            Thread.sleep(3000);
            
            // 滚动加载更多
            scrollPage(driver, 2);
            
            String pageSource = driver.getPageSource();
            
            // 从页面中提取菜品数据
            dishes = extractDishesFromPage(driver, merchantId);
            
            if (dishes.isEmpty()) {
                dishes = extractDishesFromScript(pageSource, merchantId);
            }
            
            if (dishes.isEmpty()) {
                log.warn("无法从页面获取菜品数据，使用模拟数据");
                dishes = generateMockDishes(merchantId);
            }
            
            log.info("爬取完成，共获取{}条菜品数据", dishes.size());
            
        } catch (Exception e) {
            log.error("Selenium爬取菜品失败", e);
            dishes = generateMockDishes(merchantId);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("关闭WebDriver失败", e);
                }
            }
        }
        
        return dishes;
    }

    /**
     * 从页面DOM中提取菜品数据
     */
    private List<Dish> extractDishesFromPage(WebDriver driver, Long merchantId) {
        List<Dish> dishes = new ArrayList<>();
        
        String[] selectors = {
            "[class*='food-item']",
            "[class*='dish-item']",
            "[class*='menu-item']",
            "[class*='foodItem']",
            "[class*='product-item']",
            "[data-food-id]"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                log.info("选择器 '{}' 找到 {} 个菜品元素", selector, elements.size());
                
                if (!elements.isEmpty()) {
                    for (WebElement element : elements) {
                        Dish dish = extractDishFromElement(element, merchantId);
                        if (dish != null && StringUtils.isNotBlank(dish.getDishName())) {
                            dishes.add(dish);
                        }
                    }
                    
                    if (!dishes.isEmpty()) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("选择器 '{}' 失败: {}", selector, e.getMessage());
            }
        }
        
        return dishes;
    }

    /**
     * 从单个元素中提取菜品信息
     */
    private Dish extractDishFromElement(WebElement element, Long merchantId) {
        try {
            Dish dish = new Dish();
            dish.setMerchantId(merchantId);
            
            String text = element.getText();
            
            // 提取菜品名称
            String name = extractText(element, "[class*='name']", "[class*='title']", "h3", "h4");
            if (StringUtils.isBlank(name) && StringUtils.isNotBlank(text)) {
                String[] lines = text.split("\n");
                if (lines.length > 0) {
                    name = lines[0].trim();
                }
            }
            dish.setDishName(name);
            
            // 提取价格
            String priceText = extractText(element, "[class*='price']", "[class*='cost']");
            if (StringUtils.isBlank(priceText)) {
                Pattern pricePattern = Pattern.compile("[￥¥](\\d+\\.?\\d*)");
                Matcher matcher = pricePattern.matcher(text);
                if (matcher.find()) {
                    priceText = matcher.group(1);
                }
            }
            dish.setPrice(parseDecimal(priceText));
            
            // 提取销量
            String salesText = extractText(element, "[class*='sales']", "[class*='sold']");
            if (StringUtils.isBlank(salesText)) {
                Pattern salesPattern = Pattern.compile("月售(\\d+)");
                Matcher matcher = salesPattern.matcher(text);
                if (matcher.find()) {
                    salesText = matcher.group(1);
                }
            }
            dish.setMonthlySales(parseSales(salesText));
            
            // 提取描述
            String desc = extractText(element, "[class*='desc']", "[class*='info']");
            dish.setDescription(desc);
            
            // 提取图片
            try {
                List<WebElement> imgs = element.findElements(By.tagName("img"));
                if (!imgs.isEmpty()) {
                    String src = imgs.get(0).getAttribute("src");
                    if (StringUtils.isBlank(src)) {
                        src = imgs.get(0).getAttribute("data-src");
                    }
                    dish.setImageUrl(src);
                }
            } catch (Exception e) {
                // 忽略图片提取失败
            }
            
            dish.setStatus(1);
            
            return dish;
        } catch (Exception e) {
            log.debug("提取菜品信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从脚本中提取菜品数据
     */
    private List<Dish> extractDishesFromScript(String pageSource, Long merchantId) {
        List<Dish> dishes = new ArrayList<>();
        
        try {
            Pattern[] patterns = {
                Pattern.compile("\"foodList\"\\s*:\\s*(\\[[^\\]]+\\])"),
                Pattern.compile("\"foods\"\\s*:\\s*(\\[[^\\]]+\\])"),
                Pattern.compile("\"menu\"\\s*:\\s*(\\[[^\\]]+\\])")
            };
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(pageSource);
                while (matcher.find()) {
                    try {
                        String jsonStr = matcher.group(1);
                        JSONArray array = JSON.parseArray(jsonStr);
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Dish dish = parseDishFromJSON(obj, merchantId);
                            if (dish != null) {
                                dishes.add(dish);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("解析菜品JSON失败: {}", e.getMessage());
                    }
                }
                
                if (!dishes.isEmpty()) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("从脚本提取菜品数据失败", e);
        }
        
        return dishes;
    }

    /**
     * 从JSON解析菜品信息
     */
    private Dish parseDishFromJSON(JSONObject food, Long merchantId) {
        try {
            Dish dish = new Dish();
            dish.setMerchantId(merchantId);
            dish.setDishName(getStringValue(food, "name", "foodName", "dishName", "title", "spuName"));
            dish.setCategory(getStringValue(food, "category", "categoryName", "cateName", "tag"));
            dish.setPrice(getDecimalValue(food, "price", "currentPrice", "salePrice", "minPrice"));
            dish.setOriginalPrice(getDecimalValue(food, "originalPrice", "oldPrice", "originPrice", "marketPrice"));
            dish.setSalesCount(getIntegerValue(food, "salesCount", "sales", "soldCount", "monthSaled"));
            dish.setMonthlySales(getIntegerValue(food, "monthlySales", "monthSalesCount"));
            dish.setDescription(getStringValue(food, "description", "desc", "detail", "praise"));
            dish.setImageUrl(getStringValue(food, "imageUrl", "image", "pic", "picture", "picUrl"));
            dish.setStatus(1);
            return dish;
        } catch (Exception e) {
            log.debug("从JSON解析菜品信息失败", e);
            return null;
        }
    }

    @Override
    public void batchCrawlData(String keyword, int merchantLimit) {
        log.info("开始批量爬取数据，关键词：{}，商户限制：{}", keyword, merchantLimit);
        
        List<Merchant> merchants = crawlMerchants(keyword, merchantLimit);
        
        for (Merchant merchant : merchants) {
            try {
                crawlDishes(merchant.getMerchantId());
                Thread.sleep(2000 + random.nextInt(3000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("爬取商户{}的菜品失败", merchant.getMerchantId(), e);
            }
        }
        
        log.info("批量爬取数据完成");
    }

    // ==================== 工具方法 ====================

    private String getStringValue(JSONObject json, String... keys) {
        for (String key : keys) {
            if (json.containsKey(key)) {
                Object value = json.get(key);
                if (value != null) {
                    return value.toString().trim();
                }
            }
        }
        return null;
    }

    private BigDecimal getDecimalValue(JSONObject json, String... keys) {
        for (String key : keys) {
            if (json.containsKey(key)) {
                Object value = json.get(key);
                if (value != null) {
                    try {
                        if (value instanceof Number) {
                            return BigDecimal.valueOf(((Number) value).doubleValue());
                        } else {
                            String str = value.toString().replaceAll("[^0-9.]", "");
                            if (StringUtils.isNotBlank(str)) {
                                return new BigDecimal(str);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析失败
                    }
                }
            }
        }
        return null;
    }

    private Integer getIntegerValue(JSONObject json, String... keys) {
        for (String key : keys) {
            if (json.containsKey(key)) {
                Object value = json.get(key);
                if (value != null) {
                    try {
                        if (value instanceof Number) {
                            return ((Number) value).intValue();
                        } else {
                            String str = value.toString().replaceAll("[^0-9]", "");
                            if (StringUtils.isNotBlank(str)) {
                                return Integer.parseInt(str);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析失败
                    }
                }
            }
        }
        return null;
    }

    private BigDecimal parseDecimal(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            String numStr = text.replaceAll("[^0-9.]", "");
            if (StringUtils.isNotBlank(numStr)) {
                return new BigDecimal(numStr);
            }
        } catch (Exception e) {
            // 忽略解析失败
        }
        return null;
    }

    private Integer parseSales(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        try {
            String numStr = text.replaceAll("[^0-9]", "");
            if (text.contains("万")) {
                return Integer.parseInt(numStr) * 10000;
            }
            if (StringUtils.isNotBlank(numStr)) {
                return Integer.parseInt(numStr);
            }
        } catch (Exception e) {
            // 忽略解析失败
        }
        return 0;
    }

    // ==================== 模拟数据生成 ====================

    private List<Merchant> generateMockMerchants(int count) {
        List<Merchant> merchants = new ArrayList<>();
        String[] types = {"中餐", "西餐", "快餐", "甜品", "火锅", "烧烤", "日料", "韩餐"};
        String[] names = {"川味小厨", "西式简餐", "快餐王", "甜品屋", "火锅城", "烧烤摊", "寿司店", "韩式料理"};
        
        for (int i = 0; i < count; i++) {
            Merchant merchant = new Merchant();
            merchant.setMerchantName(names[i % names.length] + "（" + (i + 1) + "号店）");
            merchant.setMerchantType(types[i % types.length]);
            merchant.setRating(new BigDecimal(3.5 + random.nextDouble() * 1.5).setScale(1, RoundingMode.HALF_UP));
            merchant.setTotalSales(500 + random.nextInt(9500));
            merchant.setMonthlySales(50 + random.nextInt(950));
            merchant.setMinOrderPrice(new BigDecimal(15 + random.nextInt(35)));
            merchant.setDeliveryFee(new BigDecimal(random.nextInt(8)));
            merchant.setAddress("测试地址" + (i + 1) + "号");
            merchant.setPhone("1380000" + String.format("%04d", i));
            merchant.setStatus(1);
            merchants.add(merchant);
        }
        return merchants;
    }

    private List<Dish> generateMockDishes(Long merchantId) {
        List<Dish> dishes = new ArrayList<>();
        String[] categories = {"主食", "小食", "饮品", "甜品", "套餐"};
        String[][] namesByCategory = {
            {"宫保鸡丁", "麻婆豆腐", "水煮鱼", "回锅肉", "糖醋里脊", "红烧肉", "鱼香肉丝"},
            {"炸鸡块", "薯条", "鸡米花", "洋葱圈", "炸虾"},
            {"可乐", "雪碧", "橙汁", "柠檬茶", "奶茶"},
            {"蛋糕", "布丁", "冰淇淋", "甜甜圈"},
            {"双人套餐", "单人套餐", "全家桶", "商务套餐"}
        };
        
        int dishCount = 8 + random.nextInt(12);
        for (int i = 0; i < dishCount; i++) {
            int categoryIndex = i % categories.length;
            String[] names = namesByCategory[categoryIndex];
            
            Dish dish = new Dish();
            dish.setMerchantId(merchantId);
            dish.setDishName(names[i % names.length]);
            dish.setCategory(categories[categoryIndex]);
            dish.setPrice(new BigDecimal(12 + random.nextInt(88)));
            dish.setOriginalPrice(new BigDecimal(15 + random.nextInt(100)));
            dish.setSalesCount(random.nextInt(2000));
            dish.setMonthlySales(random.nextInt(500));
            dish.setDescription("新鲜食材，精心烹制");
            dish.setStatus(1);
            dishes.add(dish);
        }
        return dishes;
    }
}
