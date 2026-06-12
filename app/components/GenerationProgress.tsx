'use client';

import { GenerationJobStatus } from '../types/schema';

interface GenerationProgressProps {
  status: GenerationJobStatus;
  connectionWarning: string | null;
  onStop: () => void;
}

function formatRate(recordsPerSecond: number): string {
  if (recordsPerSecond <= 0) {
    return '—';
  }
  return recordsPerSecond >= 100
    ? `${Math.round(recordsPerSecond).toLocaleString()} rec/s`
    : `${recordsPerSecond.toFixed(1)} rec/s`;
}

function statusLabel(status: GenerationJobStatus['status']): string {
  switch (status) {
    case 'RUNNING':
      return 'Generating…';
    case 'COMPLETED':
      return 'Complete';
    case 'CANCELLED':
      return 'Cancelled';
    case 'FAILED':
      return 'Failed';
    default:
      return status;
  }
}

function statusClass(status: GenerationJobStatus['status']): string {
  switch (status) {
    case 'COMPLETED':
      return 'bg-success';
    case 'CANCELLED':
      return 'bg-warning';
    case 'FAILED':
      return 'bg-danger';
    default:
      return 'bg-primary progress-bar-striped progress-bar-animated';
  }
}

export default function GenerationProgress({
  status,
  connectionWarning,
  onStop,
}: GenerationProgressProps) {
  const isRunning = status.status === 'RUNNING';

  return (
    <div className="card mt-4">
      <div className="card-header d-flex justify-content-between align-items-center">
        <h4 className="card-title mb-0">Generation Progress</h4>
        {isRunning && (
          <button type="button" className="btn btn-danger btn-sm" onClick={onStop}>
            <i className="fa fa-stop me-1"></i>Stop
          </button>
        )}
      </div>
      <div className="card-body">
        {connectionWarning && (
          <div className="alert alert-warning py-2 mb-3" role="alert">
            {connectionWarning}
          </div>
        )}

        <div className="d-flex justify-content-between mb-2">
          <span className="fw-semibold">{statusLabel(status.status)}</span>
          <span className="text-muted">{status.percentComplete}%</span>
        </div>

        <div className="progress mb-3" style={{ height: '1.25rem' }}>
          <div
            className={`progress-bar ${statusClass(status.status)}`}
            role="progressbar"
            style={{ width: `${status.percentComplete}%` }}
            aria-valuenow={status.percentComplete}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            {status.percentComplete}%
          </div>
        </div>

        <div className="row text-center">
          <div className="col-md-4">
            <div className="text-muted small">Records</div>
            <div>
              {status.completedRecords.toLocaleString()} / {status.totalRecords.toLocaleString()}
            </div>
          </div>
          <div className="col-md-4">
            <div className="text-muted small">Rate</div>
            <div>{formatRate(status.recordsPerSecond)}</div>
          </div>
          <div className="col-md-4">
            <div className="text-muted small">Status</div>
            <div>{status.status}</div>
          </div>
        </div>

        {status.message && !isRunning && (
          <div
            className={`alert mt-3 mb-0 py-2 ${
              status.status === 'FAILED'
                ? 'alert-danger'
                : status.status === 'CANCELLED'
                  ? 'alert-warning'
                  : 'alert-success'
            }`}
            role="alert"
          >
            {status.message}
          </div>
        )}
      </div>
    </div>
  );
}
