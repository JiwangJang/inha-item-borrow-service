"use client";

import Button from "@/components/utilities/Button";
import AdminContext from "@/context/AdminContext";
import { ADMIN_POSITION_TYPE } from "@/types/AdminPositionType";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useContext } from "react";

export default function UsersManagePage() {
    const { adminInfo } = useContext(AdminContext);
    const router = useRouter();

    return (
        <div className="mt-5">
            <p className="black-20px">회원관리</p>
            <div className="my-3 flex gap-2 h-50">
                <div
                    className="bg-white border border-boxBorder flex-1 rounded-xl py-5 px-4 flex flex-col justify-center items-center"
                    onClick={() => router.push("/admin/users/managers")}
                >
                    <div className="relative w-full flex-1">
                        <Image src={"/images/manager.png"} alt="이미지" objectFit="contain" fill />
                    </div>
                    <p className="bold-18px mt-1">관리자 관리</p>
                </div>
                <div
                    className="bg-white border border-boxBorder flex-1 rounded-xl py-5 px-4 flex flex-col justify-center items-center cursor-pointer"
                    onClick={() => router.push("/admin/users/borrowers")}
                >
                    <div className="relative w-full flex-1">
                        <Image src={"/images/student.png"} alt="이미지" objectFit="contain" fill />
                    </div>
                    <p className="bold-18px mt-1">대여자 관리</p>
                </div>
            </div>
            <Button
                className="w-full py-2"
                title="내정보보기"
                onClick={() => router.push("/admin/users/managers/me")}
            />
            {adminInfo?.position == ADMIN_POSITION_TYPE.PRESIDENT ? (
                <Button
                    className="mt-2 w-full py-2 bg-blue-400!"
                    title="부서관리"
                    onClick={() => router.push("/admin/division")}
                />
            ) : null}
        </div>
    );
}
