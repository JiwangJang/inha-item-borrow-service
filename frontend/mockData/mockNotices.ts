import NoticeInterface from "@/types/NoticeInterface";

export const mockNotices: NoticeInterface[] = [
    {
        id: 1,
        title: "2026년 1월 물품 대여 안내",
        content:
            "새해 첫 달 물품 대여 시스템 정상 운영을 안내드립니다. 대여 신청은 웹사이트를 통해 24시간 접수가 가능합니다.",
        authorId: "admin001",
        postedAt: new Date("2026-01-02"),
        updatedAt: new Date("2026-01-02"),
    },
    {
        id: 2,
        title: "시스템 점검 공지",
        content:
            "2026년 1월 15일 오전 2시~4시 시스템 점검이 예정되어 있습니다. 이 시간대에는 물품 대여 신청이 불가능합니다.",
        authorId: "admin002",
        postedAt: new Date("2026-01-10"),
        updatedAt: new Date("2026-01-10"),
    },
    {
        id: 3,
        title: "공학용계산기 신규 등록",
        content: "대여 가능한 공학용계산기 10대가 신규 등록되었습니다. 비상 상황이나 시험 준비 시 이용 가능합니다.",
        authorId: "admin001",
        postedAt: new Date("2026-01-20"),
        updatedAt: new Date("2026-01-20"),
    },
    {
        id: 4,
        title: "우산 대여 반납 연장 안내",
        content:
            "겨울철 우산 수요 증가에 따라 대여 기간을 기존 7일에서 14일로 연장합니다. 자세한 내용은 공지사항을 참고하세요.",
        authorId: "admin003",
        postedAt: new Date("2026-01-25"),
        updatedAt: new Date("2026-01-25"),
    },
    {
        id: 5,
        title: "손상된 물품 반납 시 처리 방법",
        content: "물품을 손상된 상태로 반납할 경우, 별도의 배상금 청구가 있을 수 있습니다. 사용 시 주의 부탁드립니다.",
        authorId: "admin002",
        postedAt: new Date("2026-02-01"),
        updatedAt: new Date("2026-02-01"),
    },
    {
        id: 6,
        title: "보조배터리 재입고 알림",
        content: "품절되었던 보조배터리가 재입고되었습니다. 수량이 제한적이므로 서둘러 신청해주시기 바랍니다.",
        authorId: "admin001",
        postedAt: new Date("2026-02-03"),
        updatedAt: new Date("2026-02-03"),
    },
    {
        id: 7,
        title: "대여 신청 거부 사유 안내",
        content:
            "과거 미반납 기록이나 물품 손상 이력이 있는 경우 대여 신청이 거부될 수 있습니다. 문의는 학생회 사무실로 부탁드립니다.",
        authorId: "admin003",
        postedAt: new Date("2026-02-05"),
        updatedAt: new Date("2026-02-05"),
    },
    {
        id: 8,
        title: "접이식우산과 장우산의 차이",
        content: "접이식우산은 휴대성이 좋고, 장우산은 내구성이 우수합니다. 용도에 맞춰 선택하여 대여 신청해주세요.",
        authorId: "admin002",
        postedAt: new Date("2026-02-06"),
        updatedAt: new Date("2026-02-06"),
    },
    {
        id: 9,
        title: "시험 기간 물품 대여 집중 안내",
        content: "시험 기간에는 공학용계산기 및 보조배터리 수요가 급증합니다. 미리 신청해두시길 권장합니다.",
        authorId: "admin001",
        postedAt: new Date("2026-02-07"),
        updatedAt: new Date("2026-02-07"),
    },
    {
        id: 10,
        title: "2월 마지막 주 이벤트 공지",
        content:
            "2월 24일~28일 물품 대여 시 학생회 선물을 증정합니다. 이 기간에 신청하신 모든 분께 감사의 선물을 드립니다.",
        authorId: "admin003",
        postedAt: new Date("2026-02-08"),
        updatedAt: new Date("2026-02-08"),
    },
];
