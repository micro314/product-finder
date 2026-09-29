import type { User } from "../types/auth";

type HeaderProps = {
  user: User | null;
  onSignIn: () => void;
  onRegister: () => void;
  onSignOut: () => void;
};

export function Header({ user, onSignIn, onRegister, onSignOut }: HeaderProps) {
  return (
    <header className="topbar">
      <a className="brand" href="/">
        <span className="brand-mark">▰</span>
        <span>
          G<span className="brand-accent">CI</span>
        </span>
      </a>
      <div className="top-actions">
        {user ? (
          <>
            <span className="user-chip">
              <span className="avatar">
                {(user.username || "U")[0].toUpperCase()}
              </span>
              {user.username}
            </span>
            <button className="text-button" onClick={onSignOut}>
              Sign out
            </button>
          </>
        ) : (
          <>
            <button className="text-button" onClick={onRegister}>
              Create account
            </button>
            <button className="button button-small" onClick={onSignIn}>
              Sign in <span>↗</span>
            </button>
          </>
        )}
      </div>
    </header>
  );
}
