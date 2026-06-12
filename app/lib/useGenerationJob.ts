'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { GenerationJobStatus, GenerationStatus, StartGenerationResponse } from '../types/schema';
import { getUserId } from './utils';

const POLL_INTERVAL_MS = 750;
const MAX_BACKOFF_MS = 5000;
const TERMINAL_STATUSES: GenerationStatus[] = ['COMPLETED', 'CANCELLED', 'FAILED'];

export interface UseGenerationJobResult {
  status: GenerationJobStatus | null;
  isRunning: boolean;
  connectionWarning: string | null;
  startGeneration: (target: string, payload: unknown) => Promise<void>;
  cancelGeneration: () => Promise<void>;
  reset: () => void;
}

async function fetchJobStatus(jobId: string): Promise<GenerationJobStatus> {
  const response = await fetch(`/api/generate/jobs/${jobId}`, {
    headers: { 'X-User-Id': getUserId() },
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err?.message || err?.error || `Status request failed (${response.status})`);
  }
  return response.json();
}

export function useGenerationJob(): UseGenerationJobResult {
  const [status, setStatus] = useState<GenerationJobStatus | null>(null);
  const [connectionWarning, setConnectionWarning] = useState<string | null>(null);
  const jobIdRef = useRef<string | null>(null);
  const pollingRef = useRef(false);
  const consecutiveErrorsRef = useRef(0);

  const reset = useCallback(() => {
    pollingRef.current = false;
    jobIdRef.current = null;
    consecutiveErrorsRef.current = 0;
    setStatus(null);
    setConnectionWarning(null);
  }, []);

  const pollUntilDone = useCallback(async (jobId: string) => {
    pollingRef.current = true;
    while (pollingRef.current) {
      try {
        const nextStatus = await fetchJobStatus(jobId);
        consecutiveErrorsRef.current = 0;
        setConnectionWarning(null);
        setStatus(nextStatus);

        if (TERMINAL_STATUSES.includes(nextStatus.status)) {
          pollingRef.current = false;
          return;
        }
      } catch (error) {
        consecutiveErrorsRef.current += 1;
        const backoff = Math.min(
          MAX_BACKOFF_MS,
          POLL_INTERVAL_MS * consecutiveErrorsRef.current
        );
        setConnectionWarning(
          'Connection interrupted while checking progress. Retrying…'
        );
        await new Promise((resolve) => setTimeout(resolve, backoff));
        continue;
      }

      await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
    }
  }, []);

  const startGeneration = useCallback(
    async (target: string, payload: unknown) => {
      reset();
      const response = await fetch(`/api/generate?target=${encodeURIComponent(target)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': getUserId(),
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err?.message || err?.error || 'Failed to start generation');
      }

      const startResponse: StartGenerationResponse = await response.json();
      if (!startResponse.jobId) {
        throw new Error('Generation did not return a job id');
      }

      jobIdRef.current = startResponse.jobId;
      setStatus({
        jobId: startResponse.jobId,
        status: startResponse.status,
        totalRecords: startResponse.totalRecords,
        completedRecords: 0,
        percentComplete: 0,
        recordsPerSecond: 0,
        startedAt: Date.now(),
        updatedAt: Date.now(),
      });

      void pollUntilDone(startResponse.jobId);
    },
    [pollUntilDone, reset]
  );

  const cancelGeneration = useCallback(async () => {
    const jobId = jobIdRef.current;
    if (!jobId) {
      return;
    }

    try {
      const response = await fetch(`/api/generate/jobs/${jobId}/cancel`, {
        method: 'POST',
        headers: { 'X-User-Id': getUserId() },
      });
      if (response.ok) {
        const nextStatus: GenerationJobStatus = await response.json();
        setStatus(nextStatus);
        setConnectionWarning(null);
      }
    } catch {
      setConnectionWarning('Unable to reach server to cancel. Retrying status checks…');
    }
  }, []);

  useEffect(() => {
    return () => {
      pollingRef.current = false;
    };
  }, []);

  return {
    status,
    isRunning: status?.status === 'RUNNING',
    connectionWarning,
    startGeneration,
    cancelGeneration,
    reset,
  };
}
