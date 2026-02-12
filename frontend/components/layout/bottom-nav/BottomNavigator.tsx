"use client";

import { usePathname } from "next/navigation";
import BottomNavigatorItem, { BottomNavigatorItemSpec } from "./BottomNavigatorItem";
import { randomInt } from "crypto";

const TOP_LEVEL_ROUTE = [
    // 대여자용 top level
    "/",
    "/borrow-list",
    "/return-list",
    "/borrower-info",
    // 관리자용 top level
    "/admin",
    "/admin/request-list",
    "/admin/to-do",
    "/admin/item",
    "/admin/borrower",
];

const BORROWER_BOTTOM_NAVIGATOR_SPECS: BottomNavigatorItemSpec[] = [
    {
        id: 2423,
        title: "메인화면",
        icon: "/home.svg",
        path: "/",
    },
    {
        id: 2321,
        title: "대여내역",
        icon: "/document_search.svg",
        path: "/borrower-list",
    },
    {
        id: 4341,
        title: "반납신청",
        icon: "/hand_package.svg",
        path: "/return-list",
    },
    {
        id: 3124,
        title: "계정정보",
        icon: "/account_circle.svg",
        path: "/borrower-info",
    },
];

const ADMIN_BOTTOM_NAVIGATOR_SPECS: BottomNavigatorItemSpec[] = [
    {
        id: 1231,
        title: "메인화면",
        icon: "/home.svg",
        path: "/admin",
    },
    {
        id: 6432,
        title: "신청목록",
        icon: "/document_search.svg",
        path: "/admin/request-list",
    },
    {
        id: 8798,
        title: "결과처리",
        icon: "/search_check_2.svg",
        path: "/admin/return-list",
    },
    {
        id: 5828,
        title: "물품관리",
        icon: "/deployed_code.svg",
        path: "/admin/item",
    },
    {
        id: 4910,
        title: "회원관리",
        icon: "/account_circle.svg",
        path: "/admin/borrower",
    },
];

export default function BottomNavigator() {
    const pathname = usePathname();
    const isAdminPage = pathname.startsWith("/admin/") || pathname == "/admin";

    if (TOP_LEVEL_ROUTE.includes(pathname))
        return (
            <div className="absolute bottom-0 h-16 w-full flex ">
                {isAdminPage
                    ? ADMIN_BOTTOM_NAVIGATOR_SPECS.map((spec) => (
                          <BottomNavigatorItem currentPath={pathname} spec={spec} key={spec.id} />
                      ))
                    : BORROWER_BOTTOM_NAVIGATOR_SPECS.map((spec) => (
                          <BottomNavigatorItem currentPath={pathname} spec={spec} key={spec.id} />
                      ))}
            </div>
        );
    return null;
}
