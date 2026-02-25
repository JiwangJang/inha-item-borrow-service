"use client";

import Image from "next/image";
import { useState } from "react";
import { useRouter, usePathname } from "next/navigation";

export default function HomeHeader() {
    const router = useRouter();
    const pathname = usePathname();

    return (
        <div className="w-full flex justify-between">
            <div
                className="flex gap-1 cursor-pointer"
                onClick={() => {
                    if (pathname.startsWith("/admin")) {
                        router.push("/admin");
                    } else {
                        router.push("/");
                    }
                }}
            >
                <div>
                    <Image src={"/images/logo.png"} width={40} height={40} alt="로고" />
                </div>
                <div className="black-12px flex items-center">
                    <span>
                        미래융합대학 <br /> 물품대여서비스
                    </span>
                </div>
            </div>
            <div
                className="relative w-10 h-10 bg-slate-100 hover:bg-slate-200 border border-boxBorder rounded-full cursor-pointer"
                onClick={() => {
                    window.location.reload();
                }}
            >
                <div className={`w-full h-full`}>
                    <Image
                        src={"/images/icons/others/refresh.svg"}
                        fill
                        style={{ objectFit: "contain" }}
                        alt="새로고침 버튼"
                    />
                </div>
            </div>
        </div>
    );
}
