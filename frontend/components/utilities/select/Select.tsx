"use client";

import FieldBox from "./FieldBox";

export default function Select({
    value,
    onChange,
    placeholder = "선택",
    options,
    disabled,
}: {
    value: string;
    onChange: (v: string) => void;
    placeholder?: string;
    options: string[];
    disabled?: boolean;
}) {
    return (
        <div
            className="relative w-full"
            style={{
                opacity: disabled ? 0.6 : 1,
            }}
        >
            <FieldBox inactive={value == ""}>
                <span className={value ? "text-black" : "text-placeholder"}>{value ? value : placeholder}</span>
            </FieldBox>

            {/* 실제 select는 위에 투명하게 덮어씌움 */}
            <select
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
                className="absolute inset-0 w-full h-full opacity-0"
                style={{
                    cursor: disabled ? "default" : "pointer",
                }}
                aria-label={placeholder}
            >
                <option value="" disabled>
                    {placeholder}
                </option>
                {options.map((option, key) => (
                    <option key={key} value={option}>
                        {option}
                    </option>
                ))}
            </select>
        </div>
    );
}
