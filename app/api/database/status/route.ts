import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
    try {
        const backendResponse = await fetch('http://localhost:8084/api/database/status');
        if (!backendResponse.ok) {
            console.log(`Status responded with: ${backendResponse.status}`);
            return NextResponse.json(
                { response: backendResponse.json() },
                { status: backendResponse.status }
            );
        }
        const result = await backendResponse.json();
        return NextResponse.json(result);
    } catch (error) {
        console.error('Error calling status endpoint:', error);
        return NextResponse.json(
            { error: 'Failed to call status API' },
            { status: 500 }
        );
    }
}
