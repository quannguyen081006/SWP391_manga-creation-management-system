-- Migration: Add ReviewTask table for SLA tracking
-- Business Rules: BR-51, BR-52
-- This table was referenced in code but missing from database schema

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ReviewTask]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[ReviewTask](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [versionId] [bigint] NOT NULL,
        [reviewerId] [bigint] NOT NULL,
        [assignedAt] [datetime] NOT NULL,
        [dueAt] [datetime] NOT NULL,
        [reviewStatus] [varchar](20) NOT NULL,
     CONSTRAINT [PK_ReviewTask] PRIMARY KEY CLUSTERED 
    (
        [id] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    
    -- Add foreign key to ManuscriptVersion
    ALTER TABLE [dbo].[ReviewTask]  WITH CHECK ADD  CONSTRAINT [FK_ReviewTask_ManuscriptVersion] FOREIGN KEY([versionId])
    REFERENCES [dbo].[ManuscriptVersion] ([id])
    ALTER TABLE [dbo].[ReviewTask] CHECK CONSTRAINT [FK_ReviewTask_ManuscriptVersion]
    
    -- Add foreign key to User (reviewer)
    ALTER TABLE [dbo].[ReviewTask]  WITH CHECK ADD  CONSTRAINT [FK_ReviewTask_User] FOREIGN KEY([reviewerId])
    REFERENCES [dbo].[User] ([id])
    ALTER TABLE [dbo].[ReviewTask] CHECK CONSTRAINT [FK_ReviewTask_User]
    
    -- Add unique constraint on versionId (one review task per manuscript version)
    ALTER TABLE [dbo].[ReviewTask] ADD CONSTRAINT [UQ_ReviewTask_Version] UNIQUE NONCLUSTERED 
    (
        [versionId] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    
    -- Add default for assignedAt
    ALTER TABLE [dbo].[ReviewTask] ADD  DEFAULT (getdate()) FOR [assignedAt]
    
    PRINT 'ReviewTask table created successfully'
END
ELSE
BEGIN
    PRINT 'ReviewTask table already exists'
END
GO
