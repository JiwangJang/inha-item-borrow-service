import { useState } from "react";
import Spinner from "./Spinner";

type ButtonProps = Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "onClick"> & {
    onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void | Promise<void>;
    loadingText?: string;
    loading?: boolean; // 외부에서 제어하고 싶으면 사용
};

export default function Button({
    title,
    onClick,
    loadingText = "로딩 중...",
    loading: loadingProp,
    disabled,
    className = "",
    ...rest
}: ButtonProps) {
    const [internalLoading, setInternalLoading] = useState(false);

    const isLoading = loadingProp ?? internalLoading;
    const isDisabled = disabled || isLoading;

    const handleClick = async (e: React.MouseEvent<HTMLButtonElement>) => {
        if (!onClick || isDisabled) return;

        try {
            // loading prop을 외부에서 제어하는 경우엔 내부 로딩을 건드리지 않음
            if (loadingProp === undefined) setInternalLoading(true);
            await onClick(e);
        } finally {
            if (loadingProp === undefined) setInternalLoading(false);
        }
    };

    return (
        <button
            type="button"
            disabled={isDisabled}
            onClick={handleClick}
            className={[
                "inline-flex items-center justify-center gap-2 rounded-md",
                "bg-black text-white cursor-pointer",
                "disabled:bg-placeholder disabled:cursor-not-allowed",
                "transition",
                className,
            ].join(" ")}
            {...rest}
        >
            {isLoading ? <Spinner size={16} thickness={4} /> : title}
        </button>
    );
}
