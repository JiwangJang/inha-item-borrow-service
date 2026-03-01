// components/utilities/InfoTable.tsx
import React from "react";

export default function InfoTable({ children }: { children: React.ReactNode }) {
    return <div className="w-full overflow-hidden rounded-md border border-boxBorder">{children}</div>;
}
