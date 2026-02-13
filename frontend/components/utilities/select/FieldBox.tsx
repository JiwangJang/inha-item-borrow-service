import Image from "next/image";
import { ReactNode } from "react";

const ACTIVE = "/images/icons/others/active";
const INACTIVE = "/images/icons/others/inactive";
export default function FieldBox({ children, inactive }: { children: ReactNode; inactive: boolean }) {
    const selectorPath = (inactive ? INACTIVE : ACTIVE) + "/keyboard_arrow_down.svg";

    return (
        <div
            className={[
                "w-full rounded-md border border-boxBorder bg-white",
                "px-3 py-4 flex items-center justify-between gap-3",
            ].join(" ")}
        >
            {children}
            <Image src={selectorPath} width={24} height={24} alt="셀렉트 아이콘" />
        </div>
    );
}
