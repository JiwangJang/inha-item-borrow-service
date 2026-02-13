import Image from "next/image";
import Link from "next/link";

export default function HomeHeader() {
    return (
        <div className="w-full flex items-center">
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
