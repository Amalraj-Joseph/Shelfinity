/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
import React, { useState } from 'react';
import {
  Grid,
  Column,
  FileUploader,
  Button,
  InlineNotification,
  ProgressBar,
  Tile
} from '@carbon/react';
import { Download } from '@carbon/icons-react';
import './BulkUpload.scss';

function BulkUpload({ onUploadComplete }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleFileChange = (e) => {
    const addedFiles = e.target.files;
    if (addedFiles && addedFiles.length > 0) {
      const selectedFile = addedFiles[0];
      if (selectedFile.type === 'text/csv') {
        setFile(selectedFile);
        setError(null);
      } else {
        setFile(null);
        setError('Please select a valid CSV file');
      }
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file first');
      return;
    }

    setUploading(true);
    setError(null);
    setResult(null);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const token = localStorage.getItem('authToken');
      const response = await fetch('/api/books/bulk-upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error('Upload failed');
      }

      const data = await response.json();
      setResult(data);
      setFile(null);
      
      if (onUploadComplete) {
        onUploadComplete(data);
      }
    } catch (err) {
      setError(err.message || 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  const downloadTemplate = () => {
    window.open('/api/books/bulk-upload/template', '_blank');
  };

  return (
    <div className="bulk-upload">
      <Grid>
        <Column sm={4} md={8} lg={16}>
          <div className="bulk-upload-header">
            <h1>Bulk Upload Books</h1>
            <p>Upload multiple books at once using a CSV file</p>
          </div>
        </Column>
      </Grid>

      <Grid>
        <Column sm={4} md={8} lg={12}>
          <Tile className="upload-section">
            <div className="template-section">
              <h3>Step 1: Download Template</h3>
              <p>Download the CSV template to see the required format:</p>
              <Button
                kind="tertiary"
                renderIcon={Download}
                onClick={downloadTemplate}
              >
                Download Template
              </Button>
            </div>

            <div className="file-upload-section">
              <h3>Step 2: Upload Your File</h3>
              <FileUploader
                accept={['.csv']}
                buttonLabel="Choose CSV File"
                filenameStatus="edit"
                iconDescription="Clear file"
                labelDescription="Only CSV files are accepted"
                labelTitle="Upload file"
                onChange={handleFileChange}
                disabled={uploading}
              />
            </div>

            {uploading && (
              <div className="upload-progress">
                <ProgressBar label="Uploading..." />
              </div>
            )}

            <Button
              onClick={handleUpload}
              disabled={!file || uploading}
              className="upload-button"
            >
              {uploading ? 'Uploading...' : 'Upload Books'}
            </Button>
          </Tile>
        </Column>

        <Column sm={4} md={8} lg={4}>
          <Tile className="instructions-section">
            <h3>CSV Format Instructions</h3>
            <p>Your CSV file should have the following columns:</p>
            <ul>
              <li><strong>title</strong> - Book title (required)</li>
              <li><strong>author</strong> - Author name (required)</li>
              <li><strong>isbn</strong> - ISBN number (optional)</li>
              <li><strong>description</strong> - Book description (optional)</li>
              <li><strong>totalCopies</strong> - Number of copies (optional, default: 1)</li>
            </ul>
            <p className="note">
              <strong>Note:</strong> The first row should contain column headers. 
              Use quotes around fields that contain commas.
            </p>
          </Tile>
        </Column>
      </Grid>

      {error && (
        <Grid>
          <Column sm={4} md={8} lg={16}>
            <InlineNotification
              kind="error"
              title="Error"
              subtitle={error}
              onCloseButtonClick={() => setError(null)}
            />
          </Column>
        </Grid>
      )}

      {result && (
        <Grid>
          <Column sm={4} md={8} lg={16}>
            <Tile className="result-section">
              <h3>Upload Results</h3>
              <div className="result-summary">
                <div className="result-stat">
                  <span className="result-label">Total Processed:</span>
                  <span className="result-value">{result.totalProcessed}</span>
                </div>
                <div className="result-stat success">
                  <span className="result-label">Successful:</span>
                  <span className="result-value">{result.successCount}</span>
                </div>
                <div className="result-stat error">
                  <span className="result-label">Failed:</span>
                  <span className="result-value">{result.errorCount}</span>
                </div>
              </div>

              {result.successMessages && result.successMessages.length > 0 && (
                <div className="messages success-messages">
                  <h4>Successfully Added:</h4>
                  <ul>
                    {result.successMessages.map((msg, index) => (
                      <li key={index}>{msg}</li>
                    ))}
                  </ul>
                </div>
              )}

              {result.errorMessages && result.errorMessages.length > 0 && (
                <div className="messages error-messages">
                  <h4>Errors:</h4>
                  <ul>
                    {result.errorMessages.map((msg, index) => (
                      <li key={index}>{msg}</li>
                    ))}
                  </ul>
                </div>
              )}
            </Tile>
          </Column>
        </Grid>
      )}
    </div>
  );
}

export default BulkUpload;

// Made with Bob
