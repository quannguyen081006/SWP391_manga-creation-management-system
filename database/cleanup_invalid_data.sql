-- Data Cleanup Script
-- Purpose: Find and delete invalid data before adding FK constraint
-- Run this script BEFORE migration_add_fk_manuscriptversion_chapter.sql

-- Find invalid manuscript versions (chapterId <= 0 or NULL)
SELECT 
    id,
    chapterId,
    version,
    status,
    createdAt,
    createdBy
FROM ManuscriptVersion
WHERE chapterId IS NULL OR chapterId <= 0
ORDER BY id;

-- Find invalid production locks (chapterId <= 0 or NULL)
SELECT 
    id,
    chapterId,
    manuscriptVersionId,
    lockedAt,
    lockedBy,
    unlockedAt
FROM ManuscriptProductionLock
WHERE chapterId IS NULL OR chapterId <= 0
ORDER BY id;

-- Count invalid manuscript versions
SELECT COUNT(*) AS invalid_manuscript_version_count
FROM ManuscriptVersion
WHERE chapterId IS NULL OR chapterId <= 0;

-- Count invalid production locks
SELECT COUNT(*) AS invalid_production_lock_count
FROM ManuscriptProductionLock
WHERE chapterId IS NULL OR chapterId <= 0;

-- ============================================================
-- DELETE STATEMENTS
-- ============================================================
-- WARNING: These DELETE statements will permanently remove data
-- Review the SELECT results above before executing DELETEs
-- ============================================================

-- Delete orphan production locks (chapterId <= 0 or NULL)
-- These locks cannot be unlocked because chapterId is invalid
DELETE FROM ManuscriptProductionLock
WHERE chapterId IS NULL OR chapterId <= 0;

-- Note: Do NOT delete ManuscriptVersion records with chapterId <= 0
-- These require manual investigation and chapterId correction
-- Contact database administrator to fix chapterId values
-- Example fix (run for each invalid record after determining correct chapterId):
-- UPDATE ManuscriptVersion SET chapterId = <correct_chapter_id> WHERE id = <invalid_record_id>;
