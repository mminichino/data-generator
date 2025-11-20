import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
    try {
        const { searchParams } = new URL(request.url);
        const sample = searchParams.get('sample') === 'true';
        const body = await request.json();
        console.log(`Generate body: ${JSON.stringify(body)} sample=${sample}`)
        const backendResponse = await fetch(`http://localhost:8084/api/generate${sample ? '?sample=true' : ''}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        if (!backendResponse.ok) {
            console.log(`Connect responded with: ${backendResponse.status}`);
            return NextResponse.json(
                { response: backendResponse.json() },
                { status: backendResponse.status }
            );
        }

        const result = await backendResponse.json();
        return NextResponse.json(result);
    } catch (error) {
        console.error('Error calling connect endpoint:', error);
        return NextResponse.json(
            { error: 'Failed to call connect API' },
            { status: 500 }
        );
    }
}
