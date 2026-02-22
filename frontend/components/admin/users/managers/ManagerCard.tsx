"use client";

import Button from "@/components/utilities/Button";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import DivisionContext from "@/context/DivisionContext";
import AdminInfoInterface from "@/types/AdminInfoInterface";
import positionConvertor from "@/utilities/positionConvertor";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function ManagerCard({ adminInfo }: { adminInfo: AdminInfoInterface }) {
    const { divisionList } = useContext(DivisionContext);
    const [viewDetailButton, setViewDetailButton] = useState(false);
    const router = useRouter();

    const division = divisionList.find(({ code }) => code == adminInfo.divisionCode);
    console.log(divisionList);

    return (
        <div
            className="bg-white border border-boxBorder rounded-xl py-3 px-4 cursor-pointer"
            onClick={() => setViewDetailButton(true)}
        >
            <p className="bold-18px">관리자 정보</p>
            <div>
                <SameSpaceRow label="이름" value={adminInfo.name} />
                <SameSpaceRow label="아이디" value={adminInfo.id} />
                <SameSpaceRow label="직급" value={positionConvertor(adminInfo.position)} />
                <SameSpaceRow label="부서" value={division?.name ?? "미정부서"} />

                <div>
                    {viewDetailButton ? (
                        <div className="flex gap-1 mt-3">
                            <Button
                                title="닫기"
                                className="py-2.5 flex-1  bold-16px bg-placeholder!"
                                onClick={(e) => {
                                    setViewDetailButton(false);
                                    // 버튼에서만 클릭 이벤트가 발동되도록 설정
                                    e.stopPropagation();
                                }}
                            />
                            <Button
                                title="세부사항보기"
                                className="py-2.5 flex-1  bold-16px"
                                onClick={() => router.push(`/admin/users/managers/${adminInfo.id}`)}
                            />
                        </div>
                    ) : (
                        <div className="flex justify-center">
                            <Image
                                src={"/images/icons/others/active/keyboard_arrow_down.svg"}
                                width={24}
                                height={24}
                                alt="버튼보기 버튼"
                            />
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
