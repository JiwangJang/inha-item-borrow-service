import Image from "next/image";
import { useRouter } from "next/navigation";

export default function NoHomeHeader({ title }: { title: string }) {
    const router = useRouter();
    return (
        <div className="relative w-full h-full flex items-center">
            <span className="pointer-events-none w-full black-24px flex-1 text-center absolute right-[50%] top-[50%] translate-x-[50%] translate-y-[-50%]">
                {title}
            </span>
            <div onClick={() => router.back()} className="cursor-pointer">
                <Image src={"/images/icons/header/arrow_back_ios.svg"} width={28} height={28} alt="뒤로가기 버튼" />
            </div>
        </div>
    );
}
