module.exports = {
  apps: [
    {
      name: 'djbh-backend',
      script: '/home/user/jdk-17.0.11+9/bin/java',
      args: '-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar /home/user/webapp/backend/target/djbh-system.jar --spring.profiles.active=dev',
      cwd: '/home/user/webapp/backend',
      env: {
        JAVA_HOME: '/home/user/jdk-17.0.11+9',
        PATH: '/home/user/jdk-17.0.11+9/bin:/usr/bin:/bin',
        LANG: 'en_US.UTF-8',
        LC_ALL: 'en_US.UTF-8'
      },
      watch: false,
      instances: 1,
      exec_mode: 'fork',
      autorestart: true,
      max_restarts: 3
    }
  ]
}
