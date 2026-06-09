package top.ortus.lightmark.backend.utils;

import org.springframework.util.StringUtils;

import java.util.List;

public final class HotelDemoDataFactory {

    private HotelDemoDataFactory() {
    }

    public static List<DemoReview> buildReviews(Long hotelId, String hotelName, String address) {
        String name = StringUtils.hasText(hotelName) ? hotelName.trim() : "这家酒店";
        String place = StringUtils.hasText(address) ? address.trim() : inferPlace(name);
        return List.of(
                new DemoReview(stableId(hotelId, 1), 5,
                        "这次住在" + name + "整体很满意，位置在" + place + "附近，白天逛完回来很方便。房间干净，没有明显异味，床品柔软，晚上也比较安静。"),
                new DemoReview(stableId(hotelId, 2), 4,
                        name + "的性价比不错，前台办理入住速度快，还会主动提醒早餐时间和周边路线。热水稳定，空调制冷也快，短住两晚很省心。"),
                new DemoReview(stableId(hotelId, 3), 5,
                        "带家人入住" + name + "体验挺好，周边吃饭和打车都方便，客房打扫细致，洗漱用品补得及时，孩子休息得不错。"),
                new DemoReview(stableId(hotelId, 4), 4,
                        name + "比较适合商务出行，网络稳定，桌面空间够用，晚上处理工作不受影响。设施维护得还可以，高峰期电梯稍微等了一会儿。"),
                new DemoReview(stableId(hotelId, 5), 3,
                        "客观说" + name + "位置和服务都还可以，房间卫生也过关。不过临街房间偶尔能听到车声，睡眠浅的人建议提前备注安静房。")
        );
    }

    public static List<String> buildReviewContents(Long hotelId, String hotelName, String address) {
        return buildReviews(hotelId, hotelName, address).stream()
                .map(DemoReview::content)
                .toList();
    }

    private static long stableId(Long hotelId, int seq) {
        long safeId = hotelId == null ? 0L : Math.abs(hotelId);
        return -(safeId * 10 + seq);
    }

    private static String inferPlace(String hotelName) {
        if (!StringUtils.hasText(hotelName)) {
            return "核心商圈";
        }
        String name = hotelName.trim();
        List<String> places = List.of(
                "王府井", "西单", "国贸", "三里屯", "外滩", "陆家嘴", "迪士尼", "珠江新城", "北京路",
                "福田", "南山", "太古里", "春熙路", "西湖", "武林", "钟楼", "大雁塔", "亚龙湾", "海棠湾"
        );
        return places.stream()
                .filter(name::contains)
                .findFirst()
                .orElse("核心商圈");
    }

    public record DemoReview(Long id, Integer rating, String content) {
    }
}
