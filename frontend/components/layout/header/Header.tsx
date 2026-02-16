"use client";

import { usePathname } from "next/navigation";
import { useMemo } from "react";
import HomeHeader from "./HomeHeader";
import NoHomeHeader from "./NoHomeHeader";

export default function Header() {
    const pathname = usePathname();

    const spec: string | null = useMemo(() => {
        if (pathname === "/borrower-request") {
            return "물품대여신청";
        } else if (pathname == "/admin/item/new") {
            return "물품등록";
        } else if (pathname == "/admin/item/revise") {
            return "물품정보수정";
        } else if (pathname.startsWith("/borrow-list/") && pathname.endsWith("/revise")) {
            return "대여신청 수정";
        } else if (pathname.startsWith("/borrow-list/")) {
            return "대여신청내역";
        } else if (pathname.startsWith("/return-list/")) {
            return "반납신청내역";
        } else if (pathname.startsWith("/borrower-info/agreement")) {
            return "개인정보 수집동의";
        } else if (pathname.startsWith("/borrower-info/me")) {
            return "내 정보확인";
        } else if (pathname.startsWith("/login")) {
            return "로그인";
        } else if (pathname.startsWith("/borrower-info/student-council-fee")) {
            return "학생회비 납부";
        } else if (pathname.startsWith("/admin/student-council")) {
            return "학생회비 납부확인";
        } else if (pathname.startsWith("/admin/to-do/borrow/") || pathname.startsWith("/admin/to-do/return/")) {
            return "세부사항보기";
        } else if (pathname.startsWith("/admin/item/")) {
            return "물품조회";
        } else if (pathname.startsWith("/admin/borrower/info/")) {
            return "회원정보조회";
        }

        // 기타 페이지는 기본 형태로 표시(HomeHeader)
        return null;
    }, [pathname]);

    return (
        <header className="absolute w-full common-px bg-white h-15 border border-b border-boxBorder flex items-center z-1">
            {spec == null ? <HomeHeader /> : <NoHomeHeader title={spec} />}
        </header>
    );
}
