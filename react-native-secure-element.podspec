require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name           = 'react-native-secure-element'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = 'https://github.com/NectGmbH/react-native-secure-element'
  s.source       = { :git => "https://github.com/NectGmbH/react-native-secure-element.git", :tag => "#{s.version}" }
  s.source_files           = "**/*.{c,h,m,mm,cpp,swift}"
  s.static_framework = true
  s.header_dir             = "."

  s.ios.deployment_target = "10.3"
  s.swift_version     = '4.2'

  s.framework              = "Foundation"
  s.pod_target_xcconfig = { 'SWIFT_VERSION' => '4.2' }

  s.dependency "React"
  s.dependency "EllipticCurveKeyPair"
end
