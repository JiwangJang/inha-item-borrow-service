import Image from "next/image";
import { useRouter } from "next/navigation";

export interface BottomNavigatorItemSpec {
    id: number;
    title: string;
    icon: string;
    path: string;
}

const ACTIVE_ICON_FOLDER = "/images/icons/bottom-bar/active";
const INACTIVE_ICON_FOLDER = "/images/icons/bottom-bar/inactive";

export default function BottomNavigatorItem({
    currentPath,
    spec,
}: {
    currentPath: string;
    spec: BottomNavigatorItemSpec;
}) {
    const router = useRouter();
    const isActive = currentPath == spec.path;
    const iconPath = (isActive ? ACTIVE_ICON_FOLDER : INACTIVE_ICON_FOLDER) + spec.icon;

    return (
        <div
            className={`py-1.5 px-3.25 border-t 
              ${isActive ? "border-black" : "border-boxBorder"}
              flex-1 flex justify-center items-center bg-white cursor-pointer`}
            onClick={() => router.push(spec.path)}
        >
            <div className="flex flex-col justify-center items-center gap-1.25">
                <Image src={iconPath} width={32} height={32} alt={spec.title} />
                <span className={`regular-12px ${isActive ? "text-balck" : "text-placeholder"}`}>{spec.title}</span>
            </div>
        </div>
    );
}
