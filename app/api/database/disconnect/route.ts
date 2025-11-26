import { NextResponse } from 'next/server';

export async function POST() {
    try {
        const backendResponse = await fetch('http://localhost:8084/api/database/disconnect', {
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
