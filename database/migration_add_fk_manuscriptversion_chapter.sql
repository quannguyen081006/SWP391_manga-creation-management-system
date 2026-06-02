-- Migration: Add FK constraint to ManuscriptVersion.chapterId
-- Purpose: Prevent chapterId <= 0 from being inserted into ManuscriptVersion table
-- This migration will FAIL if invalid data (chapterId <= 0) exists
-- Run data cleanup script BEFORE this migration

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- Check if FK constraint already exists
IF NOT EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE object_id = OBJECT_ID(N'[dbo].[FK_ManuscriptVersion_Chapter]') 
    AND parent_object_id = OBJECT_ID(N'[dbo].[ManuscriptVersion]')
)
BEGIN
    -- Add foreign key constraint to Chapter table
    -- WITH CHECK validates existing data - will fail if invalid chapterId exists
    ALTER TABLE [dbo].[ManuscriptVersion] 
    WITH CHECK ADD CONSTRAINT [FK_ManuscriptVersion_Chapter] 
    FOREIGN KEY([chapterId])
    REFERENCES [dbo].[Chapter] ([id])
    
    ALTER TABLE [dbo].[ManuscriptVersion] 
    CHECK CONSTRAINT [FK_ManuscriptVersion_Chapter]
    
    PRINT 'FK constraint FK_ManuscriptVersion_Chapter added successfully'
END
ELSE
BEGIN
    PRINT 'FK constraint FK_ManuscriptVersion_Chapter already exists'
END
GO
