"use client";

import { useState } from "react";

export default function Page() {
    const [loading, setLoading] = useState(false);

    const fakeWork = async () => {
        setLoading(true);
        try {
            await new Promise((r) => setTimeout(r, 3000));
        } finally {
            setLoading(false);
        }
    };

    return <div className="p-6">Page</div>;
}
