require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name           = 'RNSecureElement'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = 'https://github.com/reime005/react-native-secure-element'
  s.source       = { :git => "https://github.com/reime005/react-native-secure-element" }
  s.source_files           = "ios/**/*.{h,m}"
  s.requires_arc = false
  s.dependency "React"
  s.platform = :ios, "10.0"
end
