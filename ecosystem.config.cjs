module.exports = {
  apps: [
    {
      name: 'djbh-backend',
      script: '/usr/lib/jvm/java-17-openjdk-amd64/bin/java',
      args: '-jar /home/user/webapp/backend/target/djbh-system.jar --spring.profiles.active=dev',
      cwd: '/home/user/webapp/backend',
      env: {
        JAVA_HOME: '/usr/lib/jvm/java-17-openjdk-amd64',
        PATH: '/usr/lib/jvm/java-17-openjdk-amd64/bin:/usr/bin:/bin'
      },
      watch: false,
      instances: 1,
      exec_mode: 'fork',
      autorestart: true,
      max_restarts: 3
    },
    {
      name: 'djbh-frontend',
      script: 'npx',
      args: 'serve -s dist -l 3001',
      cwd: '/home/user/webapp/frontend',
      env: {
        NODE_ENV: 'production'
      },
      watch: false,
      instances: 1,
      exec_mode: 'fork'
    }
  ]
}
