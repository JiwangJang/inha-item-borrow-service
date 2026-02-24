"use client";

import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import Link from "next/link";

export default function Footer() {
    return (
        <div className="w-full common-px pt-5 pb-32 bg-[#005BAC] text-white">
            <div>
                <p className="bold-18px">서비스 정보</p>
                <SameSpaceRow label="서비스명" value="미래융합대학 물품대여서비스" />
                <SameSpaceRow label="운영주체" value="인하대학교 제4대 미래융합대학 학생회" />
                <SameSpaceRow label="책임자" value="제4대 미래융합대학 학생회장 최민성" />
                <SameSpaceRow label="위치" value="인하대학교 미래융합대학관 M-202" />
                <SameSpaceRow label="문의" value="inhafuture.4th@gmail.com" />
                {/* 구글 드라이브 링크 걸기 */}
                <Link href={""} className="underline">
                    개인정보처리방침 세부내역
                </Link>{" "}
                |{" "}
                <Link href={"/admin"} className="underline">
                    관리자페이지
                </Link>
            </div>

            <div>
                <p className="bold-18px mt-4">개발자</p>
                <p>소프트웨어융합공학과 25학번 장지왕</p>
                <p>소프트웨어융합공학과 25학번 형민재</p>
            </div>
            <p className="text-center black-20px mt-4">- 찬란히 빛날 우리의 미래 -</p>
        </div>
    );
}
