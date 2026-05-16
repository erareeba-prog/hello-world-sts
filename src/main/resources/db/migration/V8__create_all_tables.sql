-- Add missing columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_pic_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- NGOS table
CREATE TABLE IF NOT EXISTS ngos (
    ngo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    name VARCHAR(200) NOT NULL,
    registration_no VARCHAR(50) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    address TEXT NOT NULL,
    contact_phone VARCHAR(20),
    website VARCHAR(500),
    logo_url VARCHAR(500),
    city VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL,
    country VARCHAR(80) DEFAULT 'India',
    verification_status VARCHAR(20) DEFAULT 'pending'
        CHECK (verification_status IN
            ('pending','under_review','verified','rejected')),
    verification_reason TEXT,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- PROVIDERS table
CREATE TABLE IF NOT EXISTS providers (
    provider_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT UNIQUE NOT NULL,
    org_name VARCHAR(200),
    org_type VARCHAR(20)
        CHECK (org_type IN ('company','trust','individual','government')),
    gstn VARCHAR(15),
    address TEXT,
    website VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    updated_by BIGINT
);

-- NGO_MEMBERS table
CREATE TABLE IF NOT EXISTS ngo_members (
    member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ngo_id UUID NOT NULL REFERENCES ngos(ngo_id),
    user_id BIGINT NOT NULL,
    role VARCHAR(20)
        CHECK (role IN ('admin','coordinator','volunteer')),
    joined_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    invited_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    updated_by BIGINT,
    UNIQUE(ngo_id, user_id)
);

-- NEEDS table
CREATE TABLE IF NOT EXISTS needs (
    need_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ngo_id UUID NOT NULL REFERENCES ngos(ngo_id),
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(20)
        CHECK (category IN
            ('food','clothing','money','education',
             'volunteers','medical','other')),
    type VARCHAR(20),
    qty_required DECIMAL(10,2) NOT NULL CHECK (qty_required >= 0),
    qty_fulfilled DECIMAL(10,2) DEFAULT 0 CHECK (qty_fulfilled >= 0),
    status VARCHAR(20) DEFAULT 'open'
        CHECK (status IN
            ('open','partially_met','fully_met',
             'closed','expired')),
    urgency VARCHAR(10) DEFAULT 'medium'
        CHECK (urgency IN ('low','medium','high','critical')),
    deadline DATE,
    image_url VARCHAR(500),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- DONATIONS table
CREATE TABLE IF NOT EXISTS donations (
    donation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id BIGINT NOT NULL,
    need_id UUID NOT NULL REFERENCES needs(need_id),
    provider_id UUID REFERENCES providers(provider_id),
    type VARCHAR(20)
        CHECK (type IN ('kind','money','volunteer_time')),
    amount_or_qty DECIMAL(10,2) NOT NULL CHECK (amount_or_qty >= 0),
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'pending'
        CHECK (status IN
            ('pending','confirmed','in_transit',
             'delivered','cancelled')),
    transaction_ref VARCHAR(100),
    cancellation_reason VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    donated_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP,
    delivered_at TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- FULFILLMENTS table
CREATE TABLE IF NOT EXISTS fulfillments (
    fulfillment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    need_id UUID NOT NULL REFERENCES needs(need_id),
    donation_id UUID UNIQUE NOT NULL
        REFERENCES donations(donation_id),
    recorded_by BIGINT NOT NULL,
    qty_applied DECIMAL(10,2) NOT NULL CHECK (qty_applied >= 0),
    proof_url VARCHAR(500),
    notes TEXT,
    fulfilled_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    updated_by BIGINT
);

-- AUDIT_LOG table
CREATE TABLE IF NOT EXISTS audit_log (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id BIGINT NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id UUID,
    action VARCHAR(20)
        CHECK (action IN
            ('INSERT','UPDATE','DELETE',
             'STATUS_CHANGE','LOGIN','LOGOUT')),
    old_value JSON,
    new_value JSON,
    changed_fields VARCHAR(500),
    performed_at TIMESTAMP DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100)
);

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_ngos_created_by ON ngos(created_by);
CREATE INDEX IF NOT EXISTS idx_ngos_verification_status ON ngos(verification_status);
CREATE INDEX IF NOT EXISTS idx_ngos_city_state ON ngos(city, state);
CREATE INDEX IF NOT EXISTS idx_ngos_is_deleted ON ngos(is_deleted);
CREATE INDEX IF NOT EXISTS idx_needs_ngo_id ON needs(ngo_id);
CREATE INDEX IF NOT EXISTS idx_needs_category ON needs(category);
CREATE INDEX IF NOT EXISTS idx_needs_urgency ON needs(urgency);
CREATE INDEX IF NOT EXISTS idx_needs_deadline ON needs(deadline);
CREATE INDEX IF NOT EXISTS idx_needs_is_deleted ON needs(is_deleted);
CREATE INDEX IF NOT EXISTS idx_donations_donor_id ON donations(donor_id);
CREATE INDEX IF NOT EXISTS idx_donations_need_id ON donations(need_id);
CREATE INDEX IF NOT EXISTS idx_donations_provider_id ON donations(provider_id);
CREATE INDEX IF NOT EXISTS idx_donations_status ON donations(status);
CREATE INDEX IF NOT EXISTS idx_donations_donated_at ON donations(donated_at);
CREATE INDEX IF NOT EXISTS idx_donations_is_deleted ON donations(is_deleted);
CREATE INDEX IF NOT EXISTS idx_fulfillments_need_id ON fulfillments(need_id);
CREATE INDEX IF NOT EXISTS idx_fulfillments_donation_id ON fulfillments(donation_id);
CREATE INDEX IF NOT EXISTS idx_fulfillments_recorded_by ON fulfillments(recorded_by);
CREATE INDEX IF NOT EXISTS idx_audit_actor_id ON audit_log(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_table_record ON audit_log(table_name, record_id);
CREATE INDEX IF NOT EXISTS idx_audit_performed_at ON audit_log(performed_at);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_ngo_members_ngo_id ON ngo_members(ngo_id);
CREATE INDEX IF NOT EXISTS idx_ngo_members_user_id ON ngo_members(user_id);
CREATE INDEX IF NOT EXISTS idx_providers_user_id ON providers(user_id);
CREATE INDEX IF NOT EXISTS idx_providers_is_verified ON providers(is_verified);
