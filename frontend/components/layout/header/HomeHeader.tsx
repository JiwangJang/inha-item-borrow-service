"use client";

import Image from "next/image";
import { useRouter, usePathname } from "next/navigation";

export default function HomeHeader() {
    const router = useRouter();
    const pathname = usePathname();
    return (
        <div
            className="w-full flex items-center cursor-pointer"
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
            <div className="black-12px">
                <span>
                    미래융합대학 <br /> 물품대여서비스
                </span>
            </div>
        </div>
    );
}
