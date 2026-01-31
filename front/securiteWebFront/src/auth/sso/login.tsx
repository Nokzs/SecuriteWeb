import { authService } from "./ssoAuthService";

export const Login = ({
  className,
  children,
}: {
  className: string;
  children: React.ReactNode;
}) => {
  return (
    <button className={className} onClick={() => authService.login()}>
      {children}
    </button>
  );
};
