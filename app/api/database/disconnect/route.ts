import { NextResponse } from 'next/server';
import { BACKEND_HOST, BACKEND_PORT } from "@/app/config";

export async function POST() {
    try {
        const backendResponse = await fetch(`http://${BACKEND_HOST}:${BACKEND_PORT}/api/database/disconnect`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!backendResponse.ok) {
            console.log(`Disconnect responded with: ${backendResponse.status}`);
            return NextResponse.json(
                { response: backendResponse.json() },
                { status: backendResponse.status }
            );
        }

        const result = await backendResponse.json();
        return NextResponse.json(result);
    } catch (error) {
        console.error('Error calling disconnect endpoint:', error);
        return NextResponse.json(
            { error: 'Failed to call disconnect API' },
            { status: 500 }
        );
    }
}
